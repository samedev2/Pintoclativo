"""
Gera app/src/main/assets/granjacam/deteccoes.json com o detector real do sistema OLHO NO PINTO
(YOLO pinteiro.pt + ByteTrack + identidade persistente), sobre o video do app.

Usa o codigo do outro repo apenas para leitura (importa monitor.detector / monitor.identidade).

Uso:
  python tools/exportar_deteccoes_olho_no_pinto.py
  python tools/exportar_deteccoes_olho_no_pinto.py --olho "C:\\caminho\\OLHO NO PINTO" --saida outro.json
"""
from __future__ import annotations

import argparse
import json
import sys
import tempfile
from collections import Counter
from pathlib import Path

import cv2

RAIZ_APP = Path(__file__).resolve().parents[1]
OLHO_PADRAO = Path(r"C:\Users\Sameque\Downloads\OLHO NO PINTO")
VIDEO_PADRAO = RAIZ_APP / "app" / "src" / "main" / "res" / "raw" / "granjacam_pintos.mp4"
SAIDA_PADRAO = RAIZ_APP / "app" / "src" / "main" / "assets" / "granjacam" / "deteccoes.json"


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--olho", type=Path, default=OLHO_PADRAO)
    ap.add_argument("--video", type=Path, default=VIDEO_PADRAO)
    ap.add_argument("--saida", type=Path, default=SAIDA_PADRAO)
    args = ap.parse_args()

    sys.path.insert(0, str(args.olho))
    from monitor.detector import DetectorYOLO
    from monitor.eventos import Barramento
    from monitor.identidade import IdentidadePersistente

    cfg = json.loads((args.olho / "config.json").read_text(encoding="utf-8"))
    cfg["modelos"]["comportamento"] = ""
    bus = Barramento(Path(tempfile.mkdtemp(prefix="olho_logs_")))
    detector = DetectorYOLO(cfg["modelos"], args.olho, bus)
    r = cfg.get("rastreio", {})
    identidade = IdentidadePersistente(float(r.get("raio_base", 0.08)), float(r.get("raio_por_segundo", 0.15)),
                                       populacao=r.get("populacao") or {})

    cap = cv2.VideoCapture(str(args.video))
    if not cap.isOpened():
        raise SystemExit(f"Nao abriu o video: {args.video}")
    fps = cap.get(cv2.CAP_PROP_FPS) or 24.0
    n_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

    quadros = []
    for i in range(n_frames):
        ok, frame = cap.read()
        if not ok:
            break
        t = i / fps
        dets = detector.detectar(frame)
        dets = [d for d in identidade.atualizar(t, dets, w, h) if d.id is not None]
        quadros.append({"t": round(t, 3), "d": [
            [d.id, d.classe, d.conf, round(d.caixa[0], 4), round(d.caixa[1], 4),
             round(d.caixa[2], 4), round(d.caixa[3], 4)] for d in dets]})
        if i % 60 == 0:
            print(f"quadro {i}/{n_frames} ({len(dets)} aves)", flush=True)
    cap.release()

    args.saida.parent.mkdir(parents=True, exist_ok=True)
    args.saida.write_text(json.dumps({"fps": round(fps, 2), "w": w, "h": h, "quadros": quadros},
                                     separators=(",", ":")), encoding="utf-8")

    por_id = Counter()
    classe_id = {}
    for q in quadros:
        for d in q["d"]:
            por_id[d[0]] += 1
            classe_id[d[0]] = d[1]
    n = [len(q["d"]) for q in quadros]
    print(json.dumps({
        "saida": str(args.saida), "quadros": len(quadros),
        "aves_por_quadro_media": round(sum(n) / max(1, len(n)), 1),
        "ids": len(por_id), "ids_por_classe": dict(Counter(classe_id.values())),
        "ids_do_inicio_ao_fim": sum(1 for v in por_id.values() if v >= 0.95 * len(quadros)),
    }, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
