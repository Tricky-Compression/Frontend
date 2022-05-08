package ru.tricky_compression.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CompressorTest {
    private static final int LENGTH = 1000;
    Random random;

    @BeforeEach
    public void initialize() {
        random = new Random();
    }

    @Test
    public void testIdentity() {
        byte[] input = new byte[LENGTH];
        random.nextBytes(input);

        byte[] compressed = Compressor.compress(input);
        if (compressed == null) {
            fail();
        }

        byte[] output = Compressor.decompress(compressed);
        if (output == null) {
            fail();
        }

        assertEquals(input, output);
    }
}
