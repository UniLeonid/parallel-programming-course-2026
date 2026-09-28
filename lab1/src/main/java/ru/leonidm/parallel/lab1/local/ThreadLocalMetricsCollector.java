package ru.leonidm.parallel.lab1.local;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Const;
import ru.leonidm.parallel.lab1.MetricsCollector;
import ru.leonidm.parallel.lab1.Snapshot;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class ThreadLocalMetricsCollector implements MetricsCollector {

    private final List<ThreadState> allStates = new ArrayList<>();
    private final Object listLock = new Object();

    private final ThreadLocal<ThreadState> myState = ThreadLocal.withInitial(() -> {
        ThreadState s = new ThreadState();
        synchronized (listLock){
            allStates.add(s);
        }
        return s;
    });

    @Override
    public void record(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Got negative value: %s".formatted(value));
        }

        ThreadState s = myState.get();
        int b = (int) Math.min(value / 4, s.buckets.length() - 1);

        s.buckets.setRelease(b, s.buckets.getPlain(b) + 1);
        s.count.setRelease(s.count.getPlain() + 1);
        s.sum.setRelease(s.sum.getPlain() + value);

        // min и max обновляются обычным сравнением без CAS:
        if (value < s.min.getPlain()) {
            s.min.setRelease(value);
        }
        if (value > s.max.getPlain()) {
            s.max.setRelease(value);
        }
    }

    @Override
    public Snapshot snapshot() {
        List<ThreadState> copyOfStates;
        synchronized (listLock) {
            copyOfStates = new ArrayList<>(allStates);
        }

        long[] out = new long[Const.BUCKETS];
        long count = 0;
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = 0;

        for (ThreadState s : copyOfStates) {
            for (int i = 0; i < Const.BUCKETS; i++) {
                out[i] += s.buckets.get(i);
            }
            count += s.count.get();

            sum += s.sum.get();
            min = Math.min(min, s.min.get());
            max = Math.max(max, s.max.get());
        }

        return new Snapshot(out, count, sum, min, max);
    }
}
