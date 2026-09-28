from pathlib import Path

import matplotlib.pyplot as plt

threads = [1, 2, 4, 8, 12]
small = {
    "Этап 1: пустой лок": [62.8, 26.1, 19.5, 19.0, 18.9],
    "Этап 1: единый лок": [63.9, 19.9, 15.4, 13.4, 16.2],
    "Этап 2: шардирование (16 групп)": [47.8, 18.3, 16.6, 15.1, 12.8],
}
large = {
    "Этап 3: Thread-Local": [251.9, 501.8, 904.0, 1167.4, 1338.1],
    "Этап 4: двойная буферизация": [151.6, 301.4, 533.2, 738.7, 875.8],
}

fig, (ax_small, ax_large) = plt.subplots(1, 2, figsize=(14, 5.5))

for name, ys in small.items():
    ax_small.plot(threads, ys, marker="o", label=name)

colors = plt.rcParams["axes.prop_cycle"].by_key()["color"]
for (name, ys), color in zip(large.items(), colors[3:]):
    ax_large.plot(threads, ys, marker="o", label=name, color=color)
ax_large.axhline(636.5, linestyle="--", color="gray", label="Этап 0: однопоточный baseline (T=1)")
ax_large.axvline(4, linestyle=":", color="gray")
ax_large.text(4.15, 0.03, "4 P-ядра", color="gray", transform=ax_large.get_xaxis_transform())

for ax in (ax_small, ax_large):
    ax.set_ylim(bottom=0)
    ax.set_xticks(threads)
    ax.set_xlabel("Число потоков T")
    ax.set_ylabel("Млн оп/с")
    ax.grid(True, alpha=0.3)
    ax.legend()

fig.tight_layout()
fig.savefig(Path(__file__).parent / "img2.png", dpi=150)
