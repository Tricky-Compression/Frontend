package ru.tricky_compression.model;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Compressor {
    private static final int LEVEL = Zstd.maxCompressionLevel();

    public static byte[] compress(byte[] input) {
        int size = (int) Zstd.compressBound(input.length);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(size);
             ZstdOutputStream zos = new ZstdOutputStream(os).setLevel(LEVEL)) {
            zos.write(input);
            zos.close();
            return os.toByteArray();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static byte[] decompress(byte[] input) {
        int size = (int) Zstd.decompressedSize(input);
        try (ByteArrayInputStream is = new ByteArrayInputStream(input);
             ZstdInputStream zis = new ZstdInputStream(is)) {
            byte[] output = new byte[size];
            Arrays.fill(output, (byte) 0);
            if (zis.read(output) != size) {
                return null;
            }
            zis.close();
            return output;
        } catch (IOException ignored) {
            return null;
        }
    }
}
