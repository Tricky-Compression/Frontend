package ru.tricky_compression.model;

import android.util.Log;

import com.github.luben.zstd.Zstd;

public class Compressor {
    private static final int LEVEL = Zstd.maxCompressionLevel();

    public static byte[] compress(byte[] input) {
        var start = System.nanoTime();
        byte[] output = Zstd.compress(input, LEVEL);
        Log.i("compress time", String.valueOf(System.nanoTime() - start));
        return output;
    }

    public static byte[] decompress(byte[] input) {
        var start = System.nanoTime();
        int size = (int) Zstd.decompressedSize(input);
        byte[] output = Zstd.decompress(input, size);
        Log.i("decompress time", String.valueOf(System.nanoTime() - start));
        return output;
    }
}
