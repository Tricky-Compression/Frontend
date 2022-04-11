package ru.tricky_compression.compressor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.HashMap;


public class Trie {

    public class Node {
        private final HashMap<Character, Node> next;
        private final Node prev;
        private final char prevChar;
        private final int code;

        private Node(Node prev, char prevChar) {
            next = new HashMap<>();
            this.prev = prev;
            this.prevChar = prevChar;
            code = size++;
            codeToNode.add(this);
            if (prev != null) {
                firstChar.add(firstChar.get(prev.code));
            } else {
                firstChar.add('\0');
            }
        }

        public int getCode() {
            return code;
        }

        public char firstChar() {
            return firstChar.get(code);
        }

        public String getString() {
            StringBuilder result = new StringBuilder();
            for (Node node = this; node != root; node = node.prev) {
                result.append(node.prevChar);
            }
            return result.reverse().toString();
        }
    }

    private int size;
    private final ArrayList<Node> codeToNode;
    private final ArrayList<Character> firstChar;
    private final Node root;

    public Trie() {
        size = 0;
        codeToNode = new ArrayList<>();
        firstChar = new ArrayList<>();
        root = new Node(null, '\0');
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; ++i) {
            forceGoThrough(root, (char) i);
        }
    }

    public Node getNode(char c) {
        return root.next.get(c);
    }

    public Node getNode(String str) {
        Node node = root;
        for (char c : str.toCharArray()) {
            if (!node.next.containsKey(c)) {
                return null;
            }
            node = node.next.get(c);
        }
        return node;
    }

    public Node getNode(int code) {
        return code < codeToNode.size() ? codeToNode.get(code) : null;
    }

    public boolean canGoThrough(@NotNull Node node, char c) {
        return node.next.containsKey(c);
    }

    public boolean canGoThrough(@NotNull Node node, @NotNull String str) {
        Node curNode = node;
        for (char c : str.toCharArray()) {
            if (!curNode.next.containsKey(c)) {
                return false;
            }
            curNode = curNode.next.get(c);
        }
        return true;
    }

    public Node goThrough(@NotNull Node node, char c) throws NoSuchElementException {
        if (!node.next.containsKey(c)) {
            throw new NoSuchElementException();
        }
        return node.next.get(c);
    }

    public Node goThrough(@NotNull Node node, @NotNull String str) throws NoSuchElementException {
        Node curNode = node;
        for (char c : str.toCharArray()) {
            if (!curNode.next.containsKey(c)) {
                throw new NoSuchElementException();
            }
            curNode = curNode.next.get(c);
        }
        return curNode;
    }

    public Node forceGoThrough(@NotNull Node node, char c) {
        if (!node.next.containsKey(c)) {
            node.next.put(c, new Node(node, c));
        }
        return node;
    }

    public Node forceGoThrough(@NotNull Node node, @NotNull String str) {
        Node curNode = node;
        for (char c : str.toCharArray()) {
            if (!curNode.next.containsKey(c)) {
                curNode.next.put(c, new Node(curNode, c));
            }
            curNode = curNode.next.get(c);
        }
        return curNode;
    }
}
