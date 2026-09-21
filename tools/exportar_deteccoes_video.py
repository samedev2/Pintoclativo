"""
Gera app/src/main/assets/granjacam/deteccoes.json para o video de teste da cena de cima
(pintos + galinhas + comedouros), usando o detector do GranjaCam (pinteiro.pt) com correcoes
para essa camera:

  1. comedouros: os baldes amarelos viram a classe "comedouro" e as linhas verticais da direita viram
     "barra_separacao" (as duas fora da contagem de aves);
  2. filtro de falsos positivos: caixas sobre copos, contas vermelhas dos fios e objetos
     parados (comparando com o quadro de fundo: o quadro 0 do video e o cercado vazio);
  3. NMS entre classes e descarte de caixa pequena contida numa grande (cabeca/asa);
  4. classe pelo TAMANHO da ave, estavel por trilha: lado grande = galinha (inclui galo),
     lado pequeno = pinto. Nesta camera o pinto tem ~25 px e a galinha 60+ px.
  5. rastreamento (ByteTrack) sobre as deteccoes limpas + identidade persistente do GranjaCam.

Uso (ver docs/GRANJACAM.md):
  python tools/exportar_deteccoes_video.py
  python tools/exportar_deteccoes_video.py --pesos caminho/pinteiro.pt --granjacam "C:/.../PINTASILGO PROJECT"
"""
from __future__ import annotations

import argparse
import json
import statistics
import sys
from collections import Counter, defaultdict
from pathlib import Path

import cv2
import numpy as np

RAIZ_APP = Path(__file__).resolve().parents[1]
VIDEO_PADRAO = RAIZ_APP / "app" / "src" / "main" / "res" / "raw" / "granjacam_pintos.mp4"
SAIDA_PADRAO = RAIZ_APP / "app" / "src" / "main" / "assets" / "granjacam" / "deteccoes.json"
TRACKER_YAML = Path(__file__).with_name("rastreador_pinteiro_topo.yaml")
GRANJACAM_PADRAO = Path(r"C:\Users\Administrador\Downloads\PROJETOS CONSELHO TECH\PINTASILGO PROJECT")
PESOS_PADRAO = GRANJACAM_PADRAO / "modelos" / "historico" / "pinteiro_20260917-131627.pt"

LIMIAR_EXTRAS = 26
ABERTURA_EXTRAS = 13
ID_COMEDOURO_BASE = 1000  # ids >= 1000 sao comedouros (fixos); aves usam 1..999
ID_BARRA_BASE = 2000      # ids >= 2000 sao barras de separacao (fixas)
# Barras de separacao (linhas verticais da direita do cercado): caixa (x1, y1, x2, y2) em px no video 768x1024.
# Definidas a olho no quadro do cercado vazio (haste cinza, linha vermelha, fio escuro, linha vermelha da direita).
BARRAS_SEPARACAO = [(490, 110, 552, 935), (462, 5, 530, 980), (548, 0, 635, 950), (588, 5, 698, 960)]


# ---------------------------------------------------------------- mascaras de cor ----
def mascara_copos(frame):
    """Baldes/copos amarelo-laranja saturados (H 14-34, S>150, V>150)."""
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    m = cv2.inRange(hsv, (14, 150, 150), (34, 255, 255))
    return cv2.morphologyEx(m, cv2.MORPH_OPEN, np.ones((5, 5), np.uint8))


def mascara_vermelho(frame):
    """Contas vermelhas dos fios (H<=6 ou >=172, S>=140, V>=140)."""
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    return cv2.bitwise_or(cv2.inRange(hsv, (0, 140, 140), (6, 255, 255)),
                          cv2.inRange(hsv, (172, 140, 140), (180, 255, 255)))


def primeiro_plano(frame, fundo, limiar=38, abertura=7):
    """Manchas SOLIDAS que mudaram em relacao ao fundo (aves). A camera do video oscila alguns pixels,
    entao fios e contornos de copos deixam so linhas finas, que a abertura remove."""
    d = cv2.absdiff(cv2.GaussianBlur(frame, (5, 5), 0), cv2.GaussianBlur(fundo, (5, 5), 0)).max(axis=2)
    m = (d > limiar).astype(np.uint8) * 255
    return cv2.morphologyEx(m, cv2.MORPH_OPEN, np.ones((abertura, abertura), np.uint8))


# ----------------------------------------------------------------------- caixas ----
def _area(b):
    return max(0.0, b[2] - b[0]) * max(0.0, b[3] - b[1])


def _iou(a, b):
    ix = max(0.0, min(a[2], b[2]) - max(a[0], b[0]))
    iy = max(0.0, min(a[3], b[3]) - max(a[1], b[1]))
    inter = ix * iy
    ua = _area(a) + _area(b) - inter
    return inter / ua if ua > 0 else 0.0


def refinar(frame, fundo, caixas, confs, frac_copo=0.35, frac_fg=0.30, iou_nms=0.5, contido=0.7):
    """Filtra falsos positivos. Devolve [(caixa_px, conf)] ainda sem classe."""
    # Baldes: posicao FIXA, tirada do cercado vazio. A cor de cada quadro nao serve, porque pintos amarelos
    # amontoados se parecem com os baldes e teriam as caixas descartadas. A folga cobre a oscilacao da camera.
    copos = mascara_copos(fundo)
    copos_grossos = cv2.dilate(copos, np.ones((15, 15), np.uint8))
    vermelho = mascara_vermelho(frame)
    fg = primeiro_plano(frame, fundo)
    h, w = copos.shape
    itens = []
    for (x1, y1, x2, y2), c in zip(caixas, confs):
        xi1, yi1, xi2, yi2 = max(0, int(x1)), max(0, int(y1)), min(w, int(x2)), min(h, int(y2))
        if xi2 - xi1 < 4 or yi2 - yi1 < 4:
            continue
        lado = max(x2 - x1, y2 - y1)
        if float(copos[yi1:yi2, xi1:xi2].mean()) / 255.0 > frac_copo:
            continue
        cx, cy = int((x1 + x2) / 2), int((y1 + y2) / 2)
        if lado < 45 and 0 <= cx < w and 0 <= cy < h and copos_grossos[cy, cx] > 0:
            continue
        if lado < 70 and float(vermelho[yi1:yi2, xi1:xi2].mean()) / 255.0 > 0.10:
            continue
        if float(fg[yi1:yi2, xi1:xi2].mean()) / 255.0 < frac_fg:
            continue  # objeto parado (fio, cano, bebedouro): nao mudou em relacao ao cercado vazio
        itens.append([(x1, y1, x2, y2), float(c)])

    itens.sort(key=lambda t: t[1], reverse=True)
    sobra = []
    for it in itens:
        if all(_iou(it[0], o[0]) < iou_nms for o in sobra):
            sobra.append(it)

    final = []
    for it in sobra:
        a = _area(it[0])
        dentro = False
        for o in sobra:
            if o is it or _area(o[0]) <= 2.5 * a:
                continue
            ix = max(0.0, min(it[0][2], o[0][2]) - max(it[0][0], o[0][0]))
            iy = max(0.0, min(it[0][3], o[0][3]) - max(it[0][1], o[0][1]))
            if a > 0 and ix * iy / a > contido:
                dentro = True
                break
        if not dentro:
            final.append(it)
    return final


def aves_extras(frame, fundo, existentes, area_min=2200, area_max=16000, cobertura_max=0.35, max_pintos_dentro=2):
    """Aves adultas que o pinteiro.pt nao detectou (galinhas brancas, escuras ou de cor parecida com o solo):
    manchas GRANDES e solidas que mudaram em relacao ao cercado vazio e nao estao cobertas por nenhuma caixa.
    Aglomerados de pintos ficam de fora: ja tem varias caixas pequenas dentro deles."""
    # limiar menor que o do filtro: galinhas marrons e escuras tem cor parecida com o solo;
    # abertura maior: descola a ave do fio em que ela encosta.
    m = primeiro_plano(frame, fundo, limiar=LIMIAR_EXTRAS, abertura=ABERTURA_EXTRAS)
    m = cv2.morphologyEx(m, cv2.MORPH_CLOSE, np.ones((11, 11), np.uint8))
    n, _, stats, _ = cv2.connectedComponentsWithStats(m, connectivity=8)
    novas = []
    for i in range(1, n):
        x, y, w, h, area = stats[i]
        if not (area_min <= area <= area_max) or w < 35 or h < 35 or not (0.35 < w / h < 2.8) or area / (w * h) < 0.5:
            continue
        caixa = (float(x), float(y), float(x + w), float(y + h))
        coberto, pintos = 0.0, 0
        for e in existentes:
            ix = max(0.0, min(caixa[2], e[2]) - max(caixa[0], e[0]))
            iy = max(0.0, min(caixa[3], e[3]) - max(caixa[1], e[1]))
            inter = ix * iy
            coberto = max(coberto, inter / _area(caixa))
            if _area(e) > 0 and inter / _area(e) > 0.6 and max(e[2] - e[0], e[3] - e[1]) < 45:
                pintos += 1
        if coberto < cobertura_max and pintos <= max_pintos_dentro:
            novas.append(caixa)
    return novas


# ------------------------------------------------------------------- comedouros ----
def achar_comedouros(mascara, area_min=120):
    """Componentes conexos da mascara de copos -> caixas (x1,y1,x2,y2) em pixels."""
    n, _, stats, _ = cv2.connectedComponentsWithStats(mascara, connectivity=8)
    caixas = []
    for i in range(1, n):
        x, y, w, h, area = stats[i]
        if area >= area_min and 0.4 < w / max(1, h) < 2.5:
            folga = 4.0  # o aro laranja do balde fica fora da mascara amarela
            caixas.append((float(x) - folga, float(y) - folga, float(x + w) + folga, float(y + h) + folga))
    return caixas


def comedouros_no_quadro(frame, base):
    """Cada comedouro do cercado vazio (quadro 0) e procurado neste quadro; se o balde esta
    encoberto por uma ave, usa a posicao base deslocada pelo desvio mediano da camera."""
    atuais = achar_comedouros(mascara_copos(frame), area_min=60)
    achados, desvios = {}, []
    for i, b in enumerate(base):
        cx, cy = (b[0] + b[2]) / 2, (b[1] + b[3]) / 2
        melhor, dist = None, 1e9
        for a in atuais:
            ax, ay = (a[0] + a[2]) / 2, (a[1] + a[3]) / 2
            d = ((ax - cx) ** 2 + (ay - cy) ** 2) ** 0.5
            tam = abs((a[2] - a[0]) - (b[2] - b[0])) + abs((a[3] - a[1]) - (b[3] - b[1]))
            if d < dist and d < 14 and tam < 0.6 * (b[2] - b[0] + b[3] - b[1]):
                melhor, dist = a, d
        if melhor is not None:
            achados[i] = melhor
            desvios.append(((melhor[0] + melhor[2]) / 2 - cx, (melhor[1] + melhor[3]) / 2 - cy))
    dx = statistics.median(d[0] for d in desvios) if desvios else 0.0
    dy = statistics.median(d[1] for d in desvios) if desvios else 0.0
    return [achados.get(i, (b[0] + dx, b[1] + dy, b[2] + dx, b[3] + dy)) for i, b in enumerate(base)]


# ------------------------------------------------------------------------ main ----
def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--video", type=Path, default=VIDEO_PADRAO)
    ap.add_argument("--saida", type=Path, default=SAIDA_PADRAO)
    ap.add_argument("--pesos", type=Path, default=PESOS_PADRAO)
    ap.add_argument("--granjacam", type=Path, default=GRANJACAM_PADRAO,
                    help="pasta do projeto GranjaCam (usa monitor.identidade para leitura)")
    ap.add_argument("--fundo", type=int, default=0, help="indice do quadro do cercado vazio (padrao 0)")
    ap.add_argument("--conf", type=float, default=0.10)
    ap.add_argument("--imgsz", type=int, default=1280)
    ap.add_argument("--lado-adulto", type=float, default=48.0, help="lado maior (px) a partir do qual e galinha")
    ap.add_argument("--limite", type=int, default=0, help="processar so N quadros (teste)")
    args = ap.parse_args()

    from ultralytics import YOLO
    from ultralytics.engine.results import Boxes
    from ultralytics.trackers.byte_tracker import BYTETracker
    from ultralytics.utils import IterableSimpleNamespace, YAML

    sys.path.insert(0, str(args.granjacam))
    from monitor.analise import Deteccao
    from monitor.identidade import IdentidadePersistente

    cap = cv2.VideoCapture(str(args.video))
    if not cap.isOpened():
        raise SystemExit(f"Nao abriu o video: {args.video}")
    fps = cap.get(cv2.CAP_PROP_FPS) or 24.0
    n_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    if args.limite:
        n_frames = min(n_frames, args.limite)
    W = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    H = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

    cap.set(cv2.CAP_PROP_POS_FRAMES, args.fundo)
    ok, fundo = cap.read()
    if not ok:
        raise SystemExit("Nao leu o quadro de fundo")
    base_comedouros = achar_comedouros(mascara_copos(fundo))
    print(f"comedouros no cercado vazio: {len(base_comedouros)}", flush=True)
    cap.set(cv2.CAP_PROP_POS_FRAMES, 0)

    modelo = YOLO(str(args.pesos))
    tracker = BYTETracker(IterableSimpleNamespace(**YAML.load(str(TRACKER_YAML))))

    # Passo 1: detectar, filtrar e rastrear. Guarda por quadro (id_trilha, caixa_px, conf).
    quadros_aves, quadros_comedouros = [], []
    lados_por_trilha = defaultdict(list)
    for i in range(n_frames):
        ok, frame = cap.read()
        if not ok:
            break
        r = modelo.predict(frame, imgsz=args.imgsz, conf=args.conf, max_det=1000, verbose=False)[0]
        xy = r.boxes.xyxy.cpu().numpy().tolist()
        cf = r.boxes.conf.cpu().numpy().tolist()
        limpas = refinar(frame, fundo, xy, cf)
        for extra in aves_extras(frame, fundo, [c for c, _ in limpas]):
            limpas.append([extra, 0.35])
        if limpas:
            arr = np.array([[*c, s, 0.0] for c, s in limpas], dtype=np.float32)
        else:
            arr = np.zeros((0, 6), dtype=np.float32)
        trilhas = tracker.update(Boxes(arr, frame.shape[:2]), frame)
        aves = []
        for t in trilhas:
            x1, y1, x2, y2, tid, conf = t[0], t[1], t[2], t[3], int(t[4]), float(t[5])
            aves.append((tid, (float(x1), float(y1), float(x2), float(y2)), conf))
            lados_por_trilha[tid].append(max(x2 - x1, y2 - y1))
        quadros_aves.append(aves)
        quadros_comedouros.append(comedouros_no_quadro(frame, base_comedouros))
        if i % 40 == 0:
            print(f"quadro {i}/{n_frames}: {len(aves)} aves", flush=True)
    cap.release()

    # Classe estavel por trilha: mediana do lado maior ao longo da vida da trilha.
    classe_trilha = {tid: ("galinha" if statistics.median(v) >= args.lado_adulto else "pinto")
                     for tid, v in lados_por_trilha.items()}

    # Passo 2: identidade persistente (liga IDs novos a aves perdidas) e escrita.
    identidade = IdentidadePersistente(0.08, 0.15)
    saida, todos_ids, classe_id = [], Counter(), {}
    for i, aves in enumerate(quadros_aves):
        t = i / fps
        dets = [Deteccao(caixa=(b[0] / W, b[1] / H, b[2] / W, b[3] / H), conf=round(c, 2),
                         id=tid, classe=classe_trilha[tid], rotulo=classe_trilha[tid])
                for tid, b, c in aves]
        dets = [d for d in identidade.atualizar(t, dets, W, H) if d.id is not None]
        linha = [[d.id, d.classe, d.conf, round(d.caixa[0], 4), round(d.caixa[1], 4),
                  round(d.caixa[2], 4), round(d.caixa[3], 4)] for d in dets]
        for d in dets:
            todos_ids[d.id] += 1
            classe_id[d.id] = d.classe
        for k, b in enumerate(quadros_comedouros[i]):
            linha.append([ID_COMEDOURO_BASE + k, "comedouro", 1.0, round(b[0] / W, 4), round(b[1] / H, 4),
                          round(b[2] / W, 4), round(b[3] / H, 4)])
        for k, (bx1, by1, bx2, by2) in enumerate(BARRAS_SEPARACAO):
            linha.append([ID_BARRA_BASE + k, "barra_separacao", 1.0, round(bx1 / W, 4), round(by1 / H, 4),
                          round(bx2 / W, 4), round(by2 / H, 4)])
        saida.append({"t": round(t, 3), "d": linha})

    args.saida.parent.mkdir(parents=True, exist_ok=True)
    args.saida.write_text(json.dumps({"fps": round(fps, 2), "w": W, "h": H, "quadros": saida},
                                     separators=(",", ":")), encoding="utf-8")

    por_classe = [Counter(d[1] for d in q["d"]) for q in saida]
    print(json.dumps({
        "saida": str(args.saida),
        "quadros": len(saida),
        "ids_de_aves": len(todos_ids),
        "ids_por_classe": dict(Counter(classe_id.values())),
        "media_por_quadro": {c: round(sum(p[c] for p in por_classe) / len(por_classe), 1)
                             for c in ("pinto", "galinha", "comedouro", "barra_separacao")},
    }, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
