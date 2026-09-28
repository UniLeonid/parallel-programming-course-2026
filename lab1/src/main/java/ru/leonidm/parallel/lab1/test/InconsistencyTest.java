package ru.leonidm.parallel.lab1.test;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.MetricsCollector;
import ru.leonidm.parallel.lab1.Snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@NullMarked
public final class InconsistencyTest {

    private InconsistencyTest() {

    }

    public static Result run(MetricsCollector collector, long[] values, int threads, int iterations) {
        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean stop = new AtomicBoolean();
        long[] ops = new long[threads];

        List<Thread> threadsList = new ArrayList<>(threads);
        for (int k = 0; k < threads; k++) {
            int finalK = k;
            Thread thread = new Thread(() -> {
                long localCount = 0;
                int i = finalK * 1000;
                try {
                    start.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                while (!stop.get()) {
                    collector.record(values[i]);
                    localCount++;
                    i++;
                    if (i == values.length) {
                        i = 0;
                    }
                }
                ops[finalK] = localCount;
            });
            threadsList.add(thread);
            thread.start();
        }

        start.countDown();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        int greater = 0;
        int less = 0;
        for (int i = 0; i < iterations; i++) {
            Snapshot snapshot = collector.snapshot();
            long sum = Arrays.stream(snapshot.buckets()).sum();
            long count = snapshot.count();
            if (sum > count) {
                greater++;
            } else if (sum < count) {
                less++;
            }
        }

        stop.set(true);

        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        long totalOps = Arrays.stream(ops).sum();
        Snapshot snapshot = collector.snapshot();
        long countDifference = snapshot.count() - totalOps;

        return new Result(
                greater,
                less,
                (double) (greater + less) / iterations,
                countDifference
        );
    }

    public record Result(
            int greater,
            int less,
            double brokenCoefficient,
            long countDifference
    ) {

    }
}
