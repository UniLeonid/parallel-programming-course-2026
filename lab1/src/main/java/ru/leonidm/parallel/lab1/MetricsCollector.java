package ru.leonidm.parallel.lab1;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MetricsCollector {

    void record(long value);

    Snapshot snapshot();

}
