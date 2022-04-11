package ru.tricky_compression.compressor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Compressor {

    private static final String EMPTY_STRING = "";
    private static final ArrayList<Integer> EMPTY_LIST = new ArrayList<>();

    public static ArrayList<Integer> compress(@NotNull String str) {
        if (str.length() == 0) {
            return EMPTY_LIST;
        }
        final Trie storage = new Trie();
        final ArrayList<Integer> result = new ArrayList<>();
        Trie.Node node = storage.getNode(str.charAt(0));
        for (int i = 1; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (storage.canGoThrough(node, c)) {
                node = storage.goThrough(node, c);
            } else {
                result.add(node.getCode());
                storage.forceGoThrough(node, c);
                node = storage.getNode(c);
            }
        }
        result.add(node.getCode());
        return result;
    }

    public static String decompress(@NotNull ArrayList<Integer> arr) {
        if (arr.isEmpty()) {
            return EMPTY_STRING;
        }
        /*int code = 0;
        final TreeMap<Integer, String> storage = new TreeMap<>();
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; ++i) {
            storage.put(code++, "" + (char)i);
        }
        StringBuilder result = new StringBuilder();
        int x = arr.get(0);
        result.append(storage.get(x));
        for (int i = 1; i < arr.size(); ++i) {
            int y = arr.get(i);
            if (storage.containsKey(y)) {
                storage.put(code++, storage.get(x) + storage.get(y).charAt(0));
                result.append(storage.get(y));
            } else {
                storage.put(code++, storage.get(x) + storage.get(x).charAt(0));
                result.append(storage.get(x)).append(storage.get(x).charAt(0));
            }
            x = y;
        }*/
        final Trie storage = new Trie();
        StringBuilder result = new StringBuilder();
        Trie.Node node = storage.getNode(arr.get(0));
        result.append(node.getString());
        for (int i = 1; i < arr.size(); ++i) {
            Trie.Node y = storage.getNode(arr.get(i));
            if (y != null) {
                storage.forceGoThrough(node, y.firstChar());
                result.append(y.getString());
            } else {
                storage.forceGoThrough(node, node.firstChar());
                result.append(node.getString()).append(node.firstChar());
            }
            node = storage.getNode(arr.get(i));
        }
        return result.toString();
    }
}
