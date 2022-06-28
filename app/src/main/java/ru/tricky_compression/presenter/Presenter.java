package ru.tricky_compression.presenter;

import okhttp3.Response;
import ru.tricky_compression.entity.ChunkData;

public interface Presenter {
    String networkErrorText = "Check internet connection and try again";

    void onDestroy();

    void printInfo(String text);

    default void printNetworkError() {
        printInfo(networkErrorText);
    }

    void uploadSingleFile();

    void downloadSingleFile();

    void sendChunkDownloadRequest(String filename, int number);

    void afterReceivingChunk(ChunkData chunkData);

    void readFilenames();

    void writeFilenames(String[] filenames);
}
