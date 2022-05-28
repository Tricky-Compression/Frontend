package ru.tricky_compression.presenter;

public interface Presenter {
    String networkErrorText = "Check internet connection and try again";

    void onDestroy();

    void printInfo(String text);

    default void printNetworkError() {
        printInfo(networkErrorText);
    }

    void uploadSingleFile();

    void downloadSingleFile();

    void readFilenames();

    void writeFilenames(String[] filenames);
}
