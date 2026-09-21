"""
Gerador de deteccoes por movimento + cor (subtracao de fundo + dark blobs).

Le o video em res/raw/granjacam_pintos.mp4, usa o subtraidor de fundo do
OpenCV (MOG2) pra encontrar regioes que se movem E um threshold de cor
(pixels escuros = galinhas/galos; pixels brancos = pintos) pra tambem
pegar aves que estao paradas. Tracking simples por IoU/centroide pra
manter o ID em frames consecutivos.

Saida: app/src/main/assets/granjacam/deteccoes.json
"""
from __future__ import annotations
import json
import os
import math
from collections import defaultdict
from typing import List, Tuple

import cv2
import numpy as np

VIDEO = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\res\raw\granjacam_pintos.mp4"
OUT   = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\assets\granjacam\deteccoes.json"

# Limites (pixels do video 768x1024)
MIN_AREA = 500          # evita ruido miudo
MAX_AREA = 18000
MIN_W, MAX_W = 10, 260
MIN_H, MAX_H = 10, 220

# Tracking
IOU_MATCH = 0.08
DIST_MATCH_PX = 70
MAX_IDS = 50
TRACK_LIFE = 8

# Cor (HSV)
# Escuros: V baixo = galinhas/galos
# Claros amarelados: V alto e S medio = pintos
# Laranja (comedouro) e vermelho (bebedouro) sao EXCLUIDOS
DARK_V_MAX = 80
LIGHT_V_MIN = 195
LIGHT_S_MIN = 35
LIGHT_S_MAX = 170
# Mascaras de exclusao (comedouros laranja + bebedouros vermelhos + linhas de metal)
EXCL_HUE = [
    (0, 12, 130, 255),       # vermelho do bebedouro
    (10, 28, 130, 255),      # laranja do comedouro
    (170, 180, 130, 255),    # vermelho (faixa alta do hue)
]

# Semente fixa
SEED = 20260920


def classificar_bbox(frame, bbox) -> str:
    """Olha o pedaco da imagem dentro da bbox e decide a classe pela cor e tamanho."""
    x1, y1, x2, y2 = bbox
    x1 = max(0, x1); y1 = max(0, y1)
    x2 = min(frame.shape[1], x2); y2 = min(frame.shape[0], y2)
    if x2 <= x1 or y2 <= y1:
        return "galinha"
    roi = frame[y1:y2, x1:x2]
    hsv = cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
    h_, s_, v_ = cv2.split(hsv)
    mean_v = float(v_.mean())
    mean_s = float(s_.mean())
    w = x2 - x1
    h = y2 - y1
    # Pixels amarelos/claros: V alto, S moderado = pinto
    if mean_v > 165 and mean_s > 40 and mean_s < 180 and w <= 50 and h <= 50:
        return "pinto"
    # Galo: muito escuro E grande (galos no video sao pretos e os maiores)
    if mean_v < 55 and w >= 70 and h >= 50:
        return "galo"
    return "galinha"


def iou(a, b) -> float:
    ax1, ay1, ax2, ay2 = a
    bx1, by1, bx2, by2 = b
    ix1, iy1 = max(ax1, bx1), max(ay1, by1)
    ix2, iy2 = min(ax2, bx2), min(ay2, by2)
    if ix2 <= ix1 or iy2 <= iy1:
        return 0.0
    inter = (ix2 - ix1) * (iy2 - iy1)
    area_a = max(1, (ax2 - ax1) * (ay2 - ay1))
    area_b = max(1, (bx2 - bx1) * (by2 - by1))
    union = area_a + area_b - inter
    return inter / union if union > 0 else 0.0


def centroide(b):
    return ((b[0] + b[2]) / 2, (b[1] + b[3]) / 2)


def main() -> None:
    np.random.seed(SEED)
    cap = cv2.VideoCapture(VIDEO)
    if not cap.isOpened():
        raise SystemExit("Falha abrindo o video")
    fps = cap.get(cv2.CAP_PROP_FPS) or 24.0
    n_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    w_frame = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    h_frame = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    cap.release()

    cap = cv2.VideoCapture(VIDEO)
    bg = cv2.createBackgroundSubtractorMOG2(
        history=60, varThreshold=18, detectShadows=False
    )
    kernel3 = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    kernel5 = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))

    tracks: List[dict] = []
    next_id = 1
    quadros: List[dict] = []

    for fi in range(n_frames):
        ok, frame = cap.read()
        if not ok:
            break

        # 1) Mascara de movimento via MOG2
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        mask_motion = bg.apply(gray)
        mask_motion = cv2.morphologyEx(mask_motion, cv2.MORPH_OPEN, kernel3, iterations=1)
        mask_motion = cv2.morphologyEx(mask_motion, cv2.MORPH_CLOSE, kernel5, iterations=2)

        # 2) Mascara de cor: pixels escuros (galinhas/galos) + pixels claros (pintos)
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        h_, s_, v_ = cv2.split(hsv)
        # Escuros: V baixo
        mask_dark = (v_ < DARK_V_MAX).astype(np.uint8) * 255
        # Claros amarelados: V alto e S medio = pintos
        mask_light = ((v_ > LIGHT_V_MIN) & (s_ > LIGHT_S_MIN) & (s_ < LIGHT_S_MAX)).astype(np.uint8) * 255
        mask_color = cv2.bitwise_or(mask_dark, mask_light)
        # Exclui laranja (comedouro) e vermelho (bebedouro)
        mask_excl = np.zeros_like(mask_color)
        for (h_lo, h_hi, s_lo, s_hi) in EXCL_HUE:
            mask_excl = cv2.bitwise_or(mask_excl, cv2.inRange(hsv, (h_lo, s_lo, 0), (h_hi, s_hi, 255)))
        mask_color = cv2.bitwise_and(mask_color, cv2.bitwise_not(mask_excl))
        mask_color = cv2.morphologyEx(mask_color, cv2.MORPH_OPEN, kernel3, iterations=1)
        mask_color = cv2.morphologyEx(mask_color, cv2.MORPH_CLOSE, kernel5, iterations=2)

        # 3) Uniao: movimento OU cor (pega aves paradas + em movimento)
        mask = cv2.bitwise_or(mask_motion, mask_color)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel3, iterations=1)

        # 4) Connected components
        num, labels, stats, _ = cv2.connectedComponentsWithStats(mask, connectivity=8)
        detections: List[Tuple[int, int, int, int]] = []
        for ci in range(1, num):
            x, y, w, h, area = stats[ci]
            if area < MIN_AREA or area > MAX_AREA:
                continue
            if w < MIN_W or w > MAX_W or h < MIN_H or h > MAX_H:
                continue
            detections.append((x, y, x + w, y + h))

        # 5) Tracking
        used = set()
        for tr in tracks:
            best_iou = 0.0
            best_di = -1
            for di, det in enumerate(detections):
                if di in used:
                    continue
                v = iou(tr["bbox"], det)
                if v > best_iou:
                    best_iou = v
                    best_di = di
            if best_iou >= IOU_MATCH and best_di >= 0:
                tr["bbox"] = detections[best_di]
                tr["life"] = TRACK_LIFE
                used.add(best_di)
                continue
            cx, cy = centroide(tr["bbox"])
            best_d = 1e9
            best_di = -1
            for di, det in enumerate(detections):
                if di in used:
                    continue
                ddx, ddy = centroide(det)
                d = math.hypot(ddx - cx, ddy - cy)
                if d < best_d:
                    best_d = d
                    best_di = di
            if best_d <= DIST_MATCH_PX and best_di >= 0:
                tr["bbox"] = detections[best_di]
                tr["life"] = TRACK_LIFE
                used.add(best_di)

        for di, det in enumerate(detections):
            if di in used:
                continue
            if len(tracks) >= MAX_IDS:
                break
            cls = classificar_bbox(frame, det)
            tracks.append({
                "id": next_id,
                "classe": cls,
                "bbox": det,
                "life": TRACK_LIFE,
            })
            next_id += 1

        for tr in tracks:
            tr["life"] -= 1
        tracks = [t for t in tracks if t["life"] > 0]

        new_dets = []
        for t in tracks:
            x1, y1, x2, y2 = t["bbox"]
            new_dets.append([
                t["id"], t["classe"], 0.85,
                max(0, x1) / w_frame,
                max(0, y1) / h_frame,
                min(w_frame, x2) / w_frame,
                min(h_frame, y2) / h_frame,
            ])
        quadros.append({"t": round(fi / fps, 3), "d": new_dets})

    cap.release()

    payload = {"fps": round(fps, 2), "w": w_frame, "h": h_frame, "quadros": quadros}
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, separators=(",", ":"))

    ids_unicos = set()
    por_classe = defaultdict(int)
    total_dets = 0
    for q in quadros:
        for d in q["d"]:
            ids_unicos.add(d[0])
            por_classe[d[1]] += 1
            total_dets += 1
    print(json.dumps({
        "video": VIDEO,
        "out": OUT,
        "fps": fps, "w": w_frame, "h": h_frame,
        "frames": len(quadros),
        "aves_unicas_ids": len(ids_unicos),
        "dets_total": total_dets,
        "por_classe": dict(por_classe),
        "dets_por_frame_media": round(total_dets / max(1, len(quadros)), 2),
        "json_bytes": os.path.getsize(OUT),
    }, indent=2))


if __name__ == "__main__":
    main()
