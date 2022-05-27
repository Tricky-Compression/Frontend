package ru.tricky_compression.presenter;

import ru.tricky_compression.model.Model;
import ru.tricky_compression.model.ModelImpl;
import ru.tricky_compression.view.View;

public class Presenter {
    private Model model;
    private View view;

    public Presenter(View view) {
        model = new ModelImpl(this);
        this.view = view;
        System.out.println(" ----- Presenter was created ----- ");
    }

    public void onDestroy() {
        model = null;
        view = null;
    }

    public void printInfo(String text) {
        view.printInfo(text);
    }

    public void uploadSingleFile() {
        model.uploadSingleFile(view.getPath());
        view.cleanPath();
    }

    public void readFiles() {
        model.readAllFiles();
    }

    public void passFileNames(String[] filenames) {
        view.printFileNames(filenames);
    }

    public void downloadSingleFile() {
        model.downloadSingleFile(view.getPath());
        view.cleanPath();
    }
}