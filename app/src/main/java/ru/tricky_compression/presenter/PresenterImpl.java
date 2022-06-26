package ru.tricky_compression.presenter;

import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.model.Model;
import ru.tricky_compression.model.ModelImpl;
import ru.tricky_compression.view.View;

public class PresenterImpl implements Presenter {
    private Model model;
    private View view;

    public PresenterImpl(View view) {
        model = new ModelImpl(this);
        this.view = view;
        System.out.println(" ----- Presenter was created ----- ");
    }

    @Override
    public void onDestroy() {
        model = null;
        view = null;
    }

    @Override
    public void printInfo(String text) {
        view.printInfo(text);
    }

    @Override
    public void uploadSingleFile() {
        model.uploadSingleFile(view.getPath());
        view.cleanPath();
    }

    @Override
    public void downloadSingleFile() {
        model.downloadSingleFile(view.getPath());
        view.cleanPath();
    }

    @Override
    public void sendChunkDownloadRequest(String filename, int number) {
        model.downloadChunk(filename, number);
    }

    @Override
    public void afterReceivingChunk(ChunkData chunkData) {
        // view.showChunk(chunkData);
    }

    @Override
    public void readFilenames() {
        model.readAllFiles();
    }

    @Override
    public void writeFilenames(String[] filenames) {
        view.printFileNames(filenames);
    }
}
