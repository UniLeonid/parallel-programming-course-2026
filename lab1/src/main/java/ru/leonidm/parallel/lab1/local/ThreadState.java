package ru.leonidm.parallel.lab1.local;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.Const;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

@NullMarked
public final class ThreadState {

    public final AtomicLongArray buckets = new AtomicLongArray(Const.BUCKETS);
    public final AtomicLong count = new AtomicLong();
    public final AtomicLong sum = new AtomicLong();
    public final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    public final AtomicLong max = new AtomicLong(0);

}
