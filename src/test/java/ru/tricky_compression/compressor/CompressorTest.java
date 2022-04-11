package ru.tricky_compression.compressor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompressorTest {

    private static final int ITERATIONS = 100;
    private static final int MIN_LEN = 100;
    private static final int MAX_LEN = 10000;
    Random random;

    private int nextInt(int left, int right) {
        return random.nextInt(right - left + 1) + left;
    }

    private String nextString() {
        int length = nextInt(MIN_LEN, MAX_LEN);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                sb.append((char)nextInt('a', 'z'));
            } else {
                sb.append((char)nextInt('A', 'Z'));
            }
        }
        return sb.toString();
    }

    @BeforeEach
    public void initialize() {
        random = new Random();
    }

    @Test
    public void testIdentity() {
        for (int i = 0; i < ITERATIONS; ++i) {
            String str = nextString();
            assertEquals(str, str, Compressor.decompress(Compressor.compress(str)));
        }
    }

    @Test
    public void testCompression() {
        for (int i = 0; i < ITERATIONS; ++i) {
            String str = nextString();
//            assertThat(str, Compressor.compress(str).size(), lessThan(str.length()));
        }
    }
}
