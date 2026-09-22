"""Verifica estabilidade dos IDs no deteccoes.json."""
import json
from collections import defaultdict

path = r"C:\Users\Sameque\Downloads\PINTOCLATIVO\app\src\main\assets\granjacam\deteccoes.json"
with open(path, "r", encoding="utf-8") as f:
    d = json.load(f)

qs = d["quadros"]
print(f"frames totais: {len(qs)}")
print(f"fps: {d.get('fps')}, w: {d.get('w')}, h: {d.get('h')}")

# frames em que cada id aparece, e classes
ids_aparicoes = defaultdict(list)
ids_classes = defaultdict(set)
for fi, q in enumerate(qs):
    for det in q["d"]:
        iid = det[0]
        ids_aparicoes[iid].append(fi)
        ids_classes[iid].add(det[1])

print(f"\naves unicas (ids): {len(ids_aparicoes)}")
print("\nave -> classe -> #frames em que aparece -> primeiro frame -> ultimo frame")
for iid in sorted(ids_aparicoes):
    aps = ids_aparicoes[iid]
    cls = ",".join(sorted(ids_classes[iid]))
    print(f"  id={iid:>2}  classe={cls:<10}  frames={len(aps):>3}  faixa=[{aps[0]}..{aps[-1]}]")

# checa troca de classe
print("\naves que mudam de classe:")
for iid, classes in ids_classes.items():
    if len(classes) > 1:
        print(f"  id={iid}: {classes}")
print("(se nenhum aparece, todos os IDs mantem a mesma classe em todos os frames = IDs estaveis)")
