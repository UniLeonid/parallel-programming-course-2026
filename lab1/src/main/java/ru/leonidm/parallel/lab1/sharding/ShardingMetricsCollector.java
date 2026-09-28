package ru.leonidm.parallel.lab1.sharding;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Const;
import ru.leonidm.parallel.lab1.MetricsCollector;
import ru.leonidm.parallel.lab1.Snapshot;

import java.util.concurrent.atomic.AtomicLong;

@NullMarked
public class ShardingMetricsCollector implements MetricsCollector {

    private final int bucketsLength = Const.BUCKETS;
    private final long[][] bucketsGroups = new long[Const.SHARDS][bucketsLength / Const.SHARDS];
    private final AtomicLong count = new AtomicLong();
    private final AtomicLong sum = new AtomicLong();
    private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong max = new AtomicLong(0);

    @Override
    public void record(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Got negative value: %s".formatted(value));
        }

        int index = (int) Math.min(value / 4, bucketsLength - 1);
        int groupIndex = index % bucketsGroups.length;

        long[] buckets = bucketsGroups[groupIndex];
        synchronized (buckets) {
            buckets[index / Const.SHARDS]++;
        }

        count.incrementAndGet();

        sum.addAndGet(value);

        long current = min.get();
        while (value < current && !min.compareAndSet(current, value)) {
            current = min.get();
        }

        current = max.get();
        while (value > current && !max.compareAndSet(current, value)) {
            current = max.get();
        }
    }

    @Override
    public Snapshot snapshot() {
        long[] newBuckets = new long[bucketsLength];

        for (int i = 0; i < bucketsGroups.length; i++) {
            long[] buckets = bucketsGroups[i];
            synchronized (buckets) {
                for (int j = 0; j < buckets.length; j++) {
                    newBuckets[i + j * Const.SHARDS] = buckets[j];
                }
            }
        }

        return new Snapshot(newBuckets, count.get(), sum.get(), min.get(), max.get());
    }
}
