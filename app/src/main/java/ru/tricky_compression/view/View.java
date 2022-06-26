package ru.tricky_compression.view;

import ru.tricky_compression.entity.ChunkData;

public interface View {
    String networkErrorText = "Check internet connection and try again";

    default void printNetworkError() {
        printInfo(networkErrorText);
    }

    void printInfo(String text);

    String getPath();

    default void cleanPath() {
        setPath("");
    }

    void setPath(String text);

    void printFileNames(String[] toDisplay);

}
