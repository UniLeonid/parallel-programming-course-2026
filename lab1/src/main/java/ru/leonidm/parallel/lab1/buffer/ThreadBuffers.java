package ru.leonidm.parallel.lab1.buffer;

import ru.leonidm.parallel.lab1.Const;

import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadBuffers {

    public final long[][] buckets = new long[2][Const.BUCKETS];
    public final long[] count = new long[2];
    public final long[] sum = new long[2];
    public final long[] min = {Long.MAX_VALUE, Long.MAX_VALUE};
    public final long[] max = {0, 0};

    public final AtomicInteger inside = new AtomicInteger(-1);

}
