from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter

threads = [1, 2, 4, 8, 12]
data = {
    "Этап 1: пустой лок": [62.8, 26.1, 19.5, 19.0, 18.9],
    "Этап 1: единый лок": [63.9, 19.9, 15.4, 13.4, 16.2],
    "Этап 2: шардирование (16 групп)": [47.8, 18.3, 16.6, 15.1, 12.8],
    "Этап 3: Thread-Local": [251.9, 501.8, 904.0, 1167.4, 1338.1],
    "Этап 4: двойная буферизация": [151.6, 301.4, 533.2, 738.7, 875.8],
}

fig, ax = plt.subplots(figsize=(9, 6))
for name, ys in data.items():
    ax.plot(threads, ys, marker="o", label=name)

ax.axhline(636.5, linestyle="--", color="gray", label="Этап 0: однопоточный baseline (T=1)")
ax.axvline(4, linestyle=":", color="gray")
ax.text(4.15, 0.03, "4 P-ядра", color="gray", transform=ax.get_xaxis_transform())

ax.set_yscale("log")
ax.yaxis.set_major_formatter(ScalarFormatter())
ax.set_xticks(threads)
ax.set_xlabel("Число потоков T")
ax.set_ylabel("Млн оп/с (логарифмическая шкала)")
ax.grid(True, which="both", alpha=0.3)
ax.legend(loc="center right")

fig.tight_layout()
fig.savefig(Path(__file__).parent / "img1.png", dpi=150)
