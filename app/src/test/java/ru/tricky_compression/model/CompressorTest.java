package ru.tricky_compression.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Random;

public class CompressorTest {
    private static final int MAX_TESTS = 10;
    private static final int MAX_LENGTH = 1000;
    private Random random;

    @BeforeEach
    public void initialize() {
        random = new Random();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void testIdentity() {
        int length = random.nextInt(MAX_LENGTH);
        byte[] input = new byte[length];
        random.nextBytes(input);
        byte[] compressed = Compressor.compress(input);
        byte[] output = Compressor.decompress(compressed);

        assertEquals(input, output);
    }
}
