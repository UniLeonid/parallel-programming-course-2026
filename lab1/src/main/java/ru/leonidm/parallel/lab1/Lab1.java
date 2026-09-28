package ru.leonidm.parallel.lab1;

import org.jspecify.annotations.NullMarked;
import ru.leonidm.parallel.lab1.buffer.DoubleBufferMetricsCollector;
import ru.leonidm.parallel.lab1.local.ThreadLocalMetricsCollector;
import ru.leonidm.parallel.lab1.naive.EmptySynchronizedMetricsCollector;
import ru.leonidm.parallel.lab1.naive.SynchronizedMetricsCollector;
import ru.leonidm.parallel.lab1.sharding.ShardingMetricsCollector;
import ru.leonidm.parallel.lab1.single.SingleThreadMetricsCollector;
import ru.leonidm.parallel.lab1.test.InconsistencyTest;
import ru.leonidm.parallel.lab1.test.PerformanceTest;

import java.util.Scanner;

@NullMarked
public class Lab1 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String collectorName;
        if (args.length > 0) {
            collectorName = args[0];
        } else {
            collectorName = scanner.nextLine().strip();
        }

        int threads;
        if (args.length > 1) {
            threads = Integer.parseInt(args[1]);
        } else {
            threads = 1;
        }

        String testType;
        if (args.length > 2) {
            testType = args[2];
        } else {
            testType = "performance";
        }

        MetricsCollector collector = switch (collectorName.toLowerCase()) {
            case "single" -> {
                if (threads != 1) {
                    throw new IllegalArgumentException("Chosen collector support only 1 thread");
                }

                yield new SingleThreadMetricsCollector();
            }
            case "naive" -> new SynchronizedMetricsCollector();
            case "naive-empty" -> new EmptySynchronizedMetricsCollector();
            case "sharding" -> new ShardingMetricsCollector();
            case "local" -> new ThreadLocalMetricsCollector();
            case "buffer" -> new DoubleBufferMetricsCollector();
            default -> throw new IllegalArgumentException("Unknown collector name: '%s'".formatted(collectorName));
        };

        long[] values = DataGenerator.generate(1 << 20);

        switch (testType) {
            case "performance" -> {
                long result = PerformanceTest.measurePoint(collector, values, threads);
                System.out.println("Result: " + result + " ops/s");
            }
            case "inconsistency" -> {
                InconsistencyTest.Result result = InconsistencyTest.run(collector, values, threads, 10000);
                System.out.println("Result: " + result);
            }
        }
    }
}
