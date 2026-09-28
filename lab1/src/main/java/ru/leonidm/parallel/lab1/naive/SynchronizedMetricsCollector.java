package ru.leonidm.parallel.lab1.naive;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Snapshot;
import ru.leonidm.parallel.lab1.single.SingleThreadMetricsCollector;

@NullMarked
public class SynchronizedMetricsCollector extends SingleThreadMetricsCollector {

    @Override
    public synchronized void record(long value) {
        super.record(value);
    }

    @Override
    public synchronized Snapshot snapshot() {
        return super.snapshot();
    }
}
