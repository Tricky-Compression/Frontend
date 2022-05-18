package ru.tricky_compression.model;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Compressor {
    private static final int LEVEL = Zstd.maxCompressionLevel();

    public static byte[] compress(byte[] input) {
        int size = (int) Zstd.compressBound(input.length);
        try (var os = new ByteArrayOutputStream(size);
             var zos = new ZstdOutputStream(os).setLevel(LEVEL)) {
            zos.write(input);
            zos.close();
            return os.toByteArray();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static byte[] decompress(byte[] input) {
        int size = (int) Zstd.decompressedSize(input);
        try (var is = new ByteArrayInputStream(input);
             var zis = new ZstdInputStream(is)) {
            byte[] output = new byte[size];
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
