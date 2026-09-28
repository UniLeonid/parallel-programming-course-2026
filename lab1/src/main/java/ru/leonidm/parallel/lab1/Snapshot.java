package ru.leonidm.parallel.lab1;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record Snapshot(
        long[] buckets,
        long count,
        long sum,
        long min,
        long max,
        long p50,
        long p99
) {

    public Snapshot(
            long[] buckets,
            long count,
            long sum,
            long min,
            long max
    ) {
        this(
                buckets,
                count,
                sum,
                min,
                max,
                computePercentile(buckets, count, 0.50d),
                computePercentile(buckets, count, 0.99d)
        );
    }

    public static long computePercentile(long[] buckets, long count, double p) {
        double threshold = count * p;
        long acc = 0;
        for (int i = 0; i < buckets.length; i++) {
            acc += buckets[i];
            if (acc >= threshold) {
                return i * 4L;
            }
        }

        return (buckets.length - 1) * 4L;
    }
}
