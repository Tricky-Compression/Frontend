package ru.tricky_compression.compressor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.TreeMap;

public class Compressor {

    private static final String EMPTY_STRING = "";
    private static final ArrayList<Long> EMPTY_LIST = new ArrayList<>();

    public static ArrayList<Long> compress(@NotNull String str) {
        if (str.length() == 0) {
            return EMPTY_LIST;
        }
        long code = 0;
        final TreeMap<String, Long> storage = new TreeMap<>();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; ++i) {
            storage.put("" + (char)i, code++);
        }
        ArrayList<Long> result = new ArrayList<>();
        StringBuilder x = new StringBuilder("" + str.charAt(0));
        for (int i = 1; i < str.length(); ++i) {
            char y = str.charAt(i);
            if (storage.containsKey(x.toString() + y)) {
                x.append(y);
            } else {
                result.add(storage.get(x.toString()));
                storage.put(x.toString() + y, code++);
                x = new StringBuilder("" + y);
            }
        }
        result.add(storage.get(x.toString()));
        return result;
    }

    public static String decompress(@NotNull ArrayList<Long> arr) {
        if (arr.isEmpty()) {
            return EMPTY_STRING;
        }
        long code = 0;
        final TreeMap<Long, String> storage = new TreeMap<>();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; ++i) {
            storage.put(code++, "" + (char)i);
        }
        StringBuilder result = new StringBuilder();
        long x = arr.get(0);
        result.append(storage.get(x));
        for (int i = 1; i < arr.size(); ++i) {
            long y = arr.get(i);
            if (storage.containsKey(y)) {
                storage.put(code++, storage.get(x) + storage.get(y).charAt(0));
                result.append(storage.get(y));
            } else {
                storage.put(code++, storage.get(x) + storage.get(x).charAt(0));
                result.append(storage.get(x)).append(storage.get(x).charAt(0));
            }
            x = y;
        }
        return result.toString();
    }
}
