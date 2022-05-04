package ru.tricky_compression;

public interface View {
    String getPath();

    default void cleanPath() {
        setPath("");
    }

    void setPath(String text);
}
