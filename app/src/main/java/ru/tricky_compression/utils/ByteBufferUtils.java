package ru.tricky_compression.utils;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class ByteBufferUtils {
    public static byte[] toByteArray(@NonNull ByteBuffer buffer, int size) {
        byte[] array = new byte[size];
        buffer.get(array);
        return array;
    }
}
