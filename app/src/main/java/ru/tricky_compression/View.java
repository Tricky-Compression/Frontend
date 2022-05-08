package ru.tricky_compression;

public interface View {
    void printInfo(String text);

    String getPath();

    default void cleanPath() {
        setPath("");
    }

    void setPath(String text);
}
