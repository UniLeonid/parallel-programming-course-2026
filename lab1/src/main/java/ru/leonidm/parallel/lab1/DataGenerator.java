package ru.leonidm.parallel.lab1;

import org.jspecify.annotations.NullMarked;

import java.util.Random;

@NullMarked
public class DataGenerator {

    public static long[] generate(int size) {
        double[] chances = new double[1024];
        double total = 0;
        for (int i = 1; i < chances.length; i++) {
            total += 1d / Math.pow(i, 1.15d);
            chances[i] = total;
        }
        for (int i = 1; i < chances.length; i++) {
            chances[i] /= total;
        }

        Random random = new Random(0xCAFEBABEL);
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            double d = random.nextDouble();

            int index = chances.length - 1;
            for (int j = 0; j < chances.length; j++) {
                if (d <= chances[j]) {
                    index = j - 1;
                    break;
                }
            }

            values[i] = index + 1;
        }

        return values;
    }
}
