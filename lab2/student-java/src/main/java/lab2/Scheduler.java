package lab2;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BooleanSupplier;

/* Этап 1: планировщик, который ведёт настоящие потоки строго по одному (передача хода через семафоры).
 * Этап 2: обнаружение deadlock в главном цикле и корректное завершение застрявших потоков.
 * Внутреннее устройство (свои классы, семафоры, вспомогательные методы для Shared и SchedLock) придумываете сами. */
@NullMarked
public final class Scheduler {

    private static final ThreadLocal<@Nullable Worker> CURRENT = new ThreadLocal<>();

    /** Если true, каждая операция с общими данными добавляет строку в log. Выставляет runner. */
    boolean logging;
    /** Лог прогона: строки "<поток>  <операция>", например "A  lock mu", "A  balance -> 100". */
    final List<String> log = new ArrayList<>();
    /** null, если deadlock не было; иначе "held={a:A, b:B}" (этап 2). */
    @Nullable String deadlock;

    private final Semaphore schedulerTurn = new Semaphore(0);
    private final List<Worker> workers = new ArrayList<>();
    private final StringBuilder trace = new StringBuilder();

    public Scheduler() {
    }

    /** Зарегистрировать поток. Потоки получают буквы A, B, C… в порядке вызова spawn. */
    public void spawn(Runnable body) {
        workers.add(new Worker(this, workers.size(), body));
    }

    /** Запустить потоки и вести их по одному: на каждой паузе спросить strategy.pick(enabled, last). */
    public void run(Strategy strategy) {
        for (Worker w : workers) {
            w.thread = new Thread(() -> runWorker(w), "scheduler-" + w.letter);
            w.thread.start();
            schedulerTurn.acquireUninterruptibly();
        }

        int lastPicked = -1;
        while (true) {
            List<Integer> enabled = new ArrayList<>();
            boolean anyAlive = false;
            for (Worker w : workers) {
                if (!w.finished) {
                    anyAlive = true;
                }
                if (w.enabled()) {
                    enabled.add(w.id);
                }
            }

            if (!anyAlive) {
                break;
            }

            if (enabled.isEmpty()) {
                throw new UnsupportedOperationException("TODO этап 2: deadlock");
            }

            int next = strategy.pick(enabled, lastPicked);
            if (!enabled.contains(next)) {
                throw new IllegalStateException("Strategy picked #%s, however enabled %s".formatted(next, enabled));
            }

            Worker w = workers.get(next);
            trace.append(w.letter);
            lastPicked = next;

            w.readyWhen = null;
            w.turn.release();
            schedulerTurn.acquireUninterruptibly();
        }

        for (Worker w : workers) {
            try {
                w.thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }

        IllegalStateException failure = null;
        for (Worker w : workers) {
            Throwable throwable = w.failure;
            if (throwable != null) {
                if (failure == null) {
                    failure = new IllegalStateException("Some worker(s) failed");
                }
                failure.addSuppressed(throwable);
            }
        }

        if (failure != null) {
            throw failure;
        }
    }

    private void runWorker(Worker w) {
        CURRENT.set(w);
        try {
            w.body.run();
        } catch (Throwable t) {
            w.failure = t;
        } finally {
            w.finished = true;
            schedulerTurn.release();
        }
    }

    /** Трасса прогона: буквы потоков, выбранных во всех решениях подряд, например "AAAABB". */
    public String traceString() {
        return trace.toString();
    }

    private void record(Worker w, String operation) {
        if (logging) {
            log.add(w.letter + "  " + operation);
        }
    }

    // === Static ===

    /** window() из тестовых программ: пауза перед следующей операцией с общими данными обязательна. */
    public static void point() {
        Worker w = current();
        w.owner.record(w, "window");
        w.needPause = true;
    }

    /** Ждать, пока cond не станет true. До этого поток не попадает в enabled. */
    public static void await(BooleanSupplier cond) {
        Worker w = current();
        if (w.needPause || !cond.getAsBoolean()) {
            pause(w, cond);
        }
        w.owner.record(w, "await done");
        w.needPause = true;
    }

    static void pauseIfNeeded() {
        Worker w = current();
        if (w.needPause) {
            pause(w, null);
        }
        w.needPause = true;
    }

    static void log(String operation) {
        Worker w = current();
        w.owner.record(w, operation);
    }

    private static void pause(Worker w, @Nullable BooleanSupplier readyWhen) {
        w.readyWhen = readyWhen;
        w.owner.schedulerTurn.release();
        w.turn.acquireUninterruptibly();
        w.needPause = false;
    }

    private static Worker current() {
        Worker w = CURRENT.get();
        if (w == null) {
            throw new IllegalStateException("not a scheduler thread");
        }
        return w;
    }

    private static final class Worker {
        private final Semaphore turn = new Semaphore(0);
        private final Scheduler owner;
        private final int id;
        private final char letter;
        private final Runnable body;

        private @Nullable Thread thread;
        private boolean needPause = true;
        private @Nullable BooleanSupplier readyWhen;
        private boolean finished;
        private @Nullable Throwable failure;

        public Worker(Scheduler owner, int id, Runnable body) {
            this.owner = owner;
            this.id = id;
            this.letter = (char) ('A' + id);
            this.body = body;
        }

        public boolean enabled() {
            return !finished && (readyWhen == null || readyWhen.getAsBoolean());
        }
    }
}
