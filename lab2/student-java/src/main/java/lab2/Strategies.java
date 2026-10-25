package lab2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* Стратегии выбора следующего потока.
 * FirstAlive — этап 1, Replay — этап 2, RandomPick — этап 3, Dfs — этап 4 (ограничение bound — этап 5).
 * enabled — номера потоков, которые могут сделать шаг, по возрастанию; last — прошлый выбранный (-1 в начале). */
public final class Strategies {
    private Strategies() { }

    /** Всегда первый поток из enabled. */
    public static final class FirstAlive implements Strategy {
        public int pick(List<Integer> enabled, int last) {
            return enabled.getFirst();
        }
    }

    /** Проигрывает трассу: на i-м решении выбирает поток trace[i] ('A' -> 0, 'B' -> 1). */
    public static final class Replay implements Strategy {
        private final String trace;
        private int position;

        public Replay(String trace) {
            this.trace = trace;
        }

        /** Сбросить позицию в трассе перед прогоном. */
        public void beginRun() {
            position = 0;
        }

        public int pick(List<Integer> enabled, int last) {
            if (position >= trace.length()) {
                throw new IllegalStateException("trace '%s', position %s: out of bounds".formatted(trace, position));
            }

            char letter = trace.charAt(position);
            int id = letter - 'A';
            if (!enabled.contains(id)) {
                throw new IllegalStateException("trace '%s', position %s: thread '%s' is not enabled"
                        .formatted(trace, position, letter));
            }

            position++;
            return id;
        }
    }

    /** Случайный поток: enabled.get(rnd.nextInt(enabled.size())), rnd = new java.util.Random(seed). */
    public static final class RandomPick implements Strategy {
        private final Random random;

        public RandomPick(long seed) {
            this.random = new Random(seed);
        }

        public int pick(List<Integer> enabled, int last) {
            return enabled.get(random.nextInt(enabled.size()));
        }
    }

    /** Stateless DFS по дереву выборов: каждый прогон с нуля, повторяет префикс прошлого и берёт следующий вариант.
     *  bound — максимум вытеснений (этап 5); для dfs runner передаёт Integer.MAX_VALUE. */
    public static final class Dfs implements Strategy {
        private final List<Node> path = new ArrayList<>();
        private int depth;

        public Dfs(int bound) { }

        /** Подготовиться к новому прогону (позиция в пути, счётчик вытеснений). */
        public void beginRun() {
            depth = 0;
        }

        public int pick(List<Integer> enabled, int last) {
            Node node;
            if (depth < path.size()) {
                node = path.get(depth);
                if (!node.enabled.equals(enabled)) {
                    throw new IllegalStateException("bad state at depth %s: expected %s, got %s"
                            .formatted(depth, node.enabled, enabled));
                }
            } else {
                node = new Node(List.copyOf(enabled));
                path.add(node);
            }

            depth++;
            return node.enabled.get(node.chosenIndex);
        }

        /** Выбрать следующий путь; false — дерево обойдено. */
        public boolean endRun() {
            while (!path.isEmpty()) {
                Node node = path.getLast();
                if (node.chosenIndex + 1 < node.enabled.size()) {
                    node.chosenIndex++;
                    return true;
                }

                path.removeLast();
            }
            return false;
        }

        private static final class Node {
            private final List<Integer> enabled;
            private int chosenIndex;

            Node(List<Integer> enabled) {
                this.enabled = enabled;
            }
        }
    }
}
