import cv2
import json
import sys

path = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\res\raw\granjacam_pintos.mp4"
cap = cv2.VideoCapture(path)
if not cap.isOpened():
    print("ERRO: nao abriu o video", file=sys.stderr)
    sys.exit(1)
fps = cap.get(cv2.CAP_PROP_FPS)
w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
n = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
dur = (n / fps) if fps else 0
cap.release()
print(json.dumps({
    "fps": round(fps, 2),
    "w": w,
    "h": h,
    "frames": n,
    "dur_s": round(dur, 2),
    "ratio": round(w / h, 4) if h else None,
}, indent=2))
