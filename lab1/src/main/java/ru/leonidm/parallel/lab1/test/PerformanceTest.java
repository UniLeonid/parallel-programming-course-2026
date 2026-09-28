package ru.leonidm.parallel.lab1.test;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.MetricsCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@NullMarked
public final class PerformanceTest {

    private PerformanceTest() {

    }

    public static long run(MetricsCollector collector, long[] values, int threads, int seconds) {
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

        long t0 = System.nanoTime();
        start.countDown();
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        stop.set(true);
        long t1 = System.nanoTime();

        for (Thread thread : threadsList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        return (long) (Arrays.stream(ops).sum() * 1e9 / (t1 - t0));
    }

    public static long measurePoint(MetricsCollector collector, long[] values, int threads) {
        run(collector, values, threads, 5);

        List<Long> results = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            results.add(run(collector, values, threads, 5));
        }

        System.out.println(collector.snapshot().count());

        Collections.sort(results);

        return results.get(2);
    }
}
