package ru.leonidm.parallel.lab1.naive;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Snapshot;

@NullMarked
public class EmptySynchronizedMetricsCollector extends SynchronizedMetricsCollector {

    @Override
    public synchronized void record(long value) {
    }

    @Override
    public synchronized Snapshot snapshot() {
        return super.snapshot();
    }
}
