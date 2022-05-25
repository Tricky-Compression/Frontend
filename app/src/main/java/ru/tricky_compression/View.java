package ru.tricky_compression;

import java.util.List;

public interface View {
    void printInfo(String text);

    String getPath();

    default void cleanPath() {
        setPath("");
    }

    void setPath(String text);

    void printFileNames(String[] toDisplay);
}
