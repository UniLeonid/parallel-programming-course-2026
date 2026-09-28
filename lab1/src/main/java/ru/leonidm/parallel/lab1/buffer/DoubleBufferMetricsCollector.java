package ru.leonidm.parallel.lab1.buffer;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Const;
import ru.leonidm.parallel.lab1.MetricsCollector;
import ru.leonidm.parallel.lab1.Snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NullMarked
public class DoubleBufferMetricsCollector implements MetricsCollector {

    private final List<ThreadBuffers> allBuffers = new ArrayList<>();
    private final Object snapLock = new Object();
    private final ThreadLocal<ThreadBuffers> myBuffer = ThreadLocal.withInitial(() -> {
        ThreadBuffers b = new ThreadBuffers();
        synchronized (snapLock) {
            allBuffers.add(b);
        }
        return b;
    });

    private volatile int active = 0;

    private final long[] globalBuckets = new long[Const.BUCKETS];
    private long globalCount = 0;
    private long globalSum = 0;
    private long globalMin = Long.MAX_VALUE;
    private long globalMax = 0;

    @Override
    public void record(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Got negative value: %s".formatted(value));
        }

        ThreadBuffers my = myBuffer.get();
        int b;
        while (true) {
            b = active;
            my.inside.set(b);
            if (active == b) {
                break;
            }
            my.inside.setRelease(-1);
        }

        int bucket = (int) Math.min(value / 4, Const.BUCKETS - 1);
        my.buckets[b][bucket]++;
        my.count[b]++;

        my.sum[b] += value;
        if (value < my.min[b]) {
            my.min[b] = value;
        }
        if (value > my.max[b]) {
            my.max[b] = value;
        }

        my.inside.setRelease(-1);
    }

    @Override
    public Snapshot snapshot() {
        synchronized (snapLock) {
            int old = active;
            active = 1 - old;

            for (ThreadBuffers s : allBuffers) {
                while (s.inside.get() == old) {
                    Thread.onSpinWait();
                }

                globalCount += s.count[old];
                globalSum += s.sum[old];
                globalMin = Math.min(globalMin, s.min[old]);
                globalMax = Math.max(globalMax, s.max[old]);

                for (int i = 0; i < Const.BUCKETS; i++) {
                    globalBuckets[i] += s.buckets[old][i];
                }

                s.count[old] = 0;
                s.sum[old] = 0;
                Arrays.fill(s.buckets[old], 0);
                s.min[old] = Long.MAX_VALUE;
                s.max[old] = 0;
            }

            return new Snapshot(
                    Arrays.copyOf(globalBuckets, globalBuckets.length),
                    globalCount,
                    globalSum,
                    globalMin,
                    globalMax
            );
        }
    }
}
