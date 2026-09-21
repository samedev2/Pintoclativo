"""
Gerador deterministico de mocks de deteccao para GranjaCam.

Le a resolucao/duracao do video em res/raw/granjacam_pintos.mp4 e gera um
assets/granjacam/deteccoes.json com caixas sinteticas mas consistentes:
  - IDs estaveis por ave (identidade persistente)
  - Trajetorias suaves (Perlin-like via soma de senoides + drift)
  - Classes: pinto (pequeno, ~70%), galinha (medio, ~25%), galo (grande/preto, ~5%)
  - Entrada/saida gradual de aves (nao todas no frame 0, nao todas no final)
  - Repulsao leve das bordas para nao encavalar

Saida: app/src/main/assets/granjacam/deteccoes.json
       (sobrescreve; mesmo schema que o original).
"""
from __future__ import annotations
import json
import math
import os
import random
from dataclasses import dataclass, field
from typing import List, Tuple

VIDEO = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\res\raw\granjacam_pintos.mp4"
OUT   = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\assets\granjacam\deteccoes.json"

# Tuning do mock
N_NAVES = 28          # numero total de identidades rastreadas
N_PINTO = 18          # quantas sao pintos (pequenas)
N_GALINHA = 8         # quantas sao galinhas
N_GALO = 2            # quantas sao galos (grandes, escuros)
FPS = 24.0
W = 768
H = 1024

# Para o Perlin-like
PERSISTENCE = 0.55
OCTAVES = 3

# Semente fixa => resultado deterministico/reproduzivel
SEED = 20260920


@dataclass
class Ave:
    id: int
    classe: str
    bx: float
    by: float
    vx: float
    vy: float
    bw: float
    bh: float
    amps: List[float] = field(default_factory=list)
    phases: List[float] = field(default_factory=list)
    t_in: int = 0
    t_out: int = 0


def smooth_noise(t: float, phases: List[float], amps: List[float], octaves: int) -> Tuple[float, float]:
    """Soma de senoides 2D - pseudo-Perlin barato e estavel."""
    dx = 0.0
    dy = 0.0
    amp = 1.0
    freq = 1.0
    for o in range(octaves):
        dx += amp * math.sin(t * 0.31 * freq + phases[o * 2])
        dy += amp * math.cos(t * 0.27 * freq + phases[o * 2 + 1])
        amp *= PERSISTENCE
        freq *= 1.7
    return dx * amps[0], dy * amps[1]


def main() -> None:
    import cv2
    cap = cv2.VideoCapture(VIDEO)
    if not cap.isOpened():
        raise SystemExit("Falha abrindo o video")
    fps = cap.get(cv2.CAP_PROP_FPS) or FPS
    n_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)) or W
    h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)) or H
    cap.release()

    rng = random.Random(SEED)
    random.seed(SEED)

    classes_plan = (["pinto"] * N_PINTO) + (["galinha"] * N_GALINHA) + (["galo"] * N_GALO)
    random.shuffle(classes_plan)

    aves: List[Ave] = []
    for idx, classe in enumerate(classes_plan, start=1):
        if classe == "pinto":
            bw = rng.uniform(0.022, 0.045)
            bh = bw * rng.uniform(0.85, 1.10)
            vmag = rng.uniform(0.0008, 0.0024)
        elif classe == "galinha":
            bw = rng.uniform(0.05, 0.08)
            bh = bw * rng.uniform(0.62, 0.80)
            vmag = rng.uniform(0.0010, 0.0028)
        else:  # galo
            bw = rng.uniform(0.07, 0.10)
            bh = bw * rng.uniform(0.65, 0.85)
            vmag = rng.uniform(0.0009, 0.0020)

        ang = rng.uniform(0, 2 * math.pi)
        aves.append(
            Ave(
                id=idx,
                classe=classe,
                bx=rng.uniform(0.08, 0.92),
                by=rng.uniform(0.08, 0.92),
                vx=math.cos(ang) * vmag,
                vy=math.sin(ang) * vmag,
                bw=bw,
                bh=bh,
                amps=[rng.uniform(0.02, 0.06), rng.uniform(0.02, 0.06)],
                phases=[rng.uniform(0, 2 * math.pi) for _ in range(OCTAVES * 2)],
                # algumas aves entram depois, outras saem antes
                t_in=rng.randint(0, int(n_frames * 0.15)),
                t_out=rng.randint(int(n_frames * 0.85), n_frames - 1),
            )
        )

    # tempo em "segundos" para o Perlin-like (a velocidade do relogio do mock)
    T_PER_FRAME = 1.0 / fps

    quadros: List[dict] = []
    for f in range(n_frames):
        dets: List[list] = []
        for a in aves:
            if f < a.t_in or f > a.t_out:
                continue
            # drift linear + oscilacao suave
            nx = a.bx + a.vx * f
            ny = a.by + a.vy * f
            ox, oy = smooth_noise(f * T_PER_FRAME, a.phases, a.amps, OCTAVES)
            x1 = nx + ox
            y1 = ny + oy
            # se sair da tela, reentra pelo lado oposto (wrap suave)
            x1 = (x1 + 1.0) % 1.0
            y1 = (y1 + 1.0) % 1.0
            # margem minima para nao encostar na borda
            x1 = max(0.01, min(0.99 - a.bw, x1))
            y1 = max(0.01, min(0.99 - a.bh, y1))
            x2 = x1 + a.bw
            y2 = y1 + a.bh
            conf = round(rng.uniform(0.78, 0.97), 2)
            dets.append([a.id, a.classe, conf,
                         round(x1, 4), round(y1, 4),
                         round(x2, 4), round(y2, 4)])
        quadros.append({"t": round(f / fps, 3), "d": dets})

    payload = {
        "fps": round(fps, 2),
        "w": w,
        "h": h,
        "quadros": quadros,
    }

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(payload, f, separators=(",", ":"))
    size = os.path.getsize(OUT)
    print(json.dumps({
        "video": VIDEO,
        "out": OUT,
        "fps": fps,
        "w": w, "h": h,
        "frames": n_frames,
        "dur_s": round(n_frames / fps, 2),
        "aves_total": len(aves),
        "json_bytes": size,
    }, indent=2))


if __name__ == "__main__":
    main()
