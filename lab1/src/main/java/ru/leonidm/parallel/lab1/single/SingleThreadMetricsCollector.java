package ru.leonidm.parallel.lab1.single;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Const;
import ru.leonidm.parallel.lab1.MetricsCollector;
import ru.leonidm.parallel.lab1.Snapshot;

import java.util.Arrays;

@NullMarked
public class SingleThreadMetricsCollector implements MetricsCollector {

    private final long[] buckets = new long[Const.BUCKETS];
    private long count = 0;
    private long sum = 0;
    private long min = Long.MAX_VALUE;
    private long max = 0;

    @Override
    public void record(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Got negative value: %s".formatted(value));
        }

        int index = (int) Math.min(value / 4, buckets.length - 1);

        buckets[index]++;
        count++;

        sum += value;
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }

    @Override
    public Snapshot snapshot() {
        return new Snapshot(
                Arrays.copyOf(buckets, buckets.length),
                count,
                sum,
                min,
                max
        );
    }
}
