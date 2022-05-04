package ru.tricky_compression;

public class Presenter {
    private final Model model;
    private View view;

    public Presenter(Model model, View view) {
        this.model = model;
        this.view = view;
        System.out.println(" ----- Presenter was created ----- ");
    }

    public void onDestroy() {
        view = null;
    }

    public void sendGreeting() {
        model.sendGreeting();
    }

    public void uploadSingleFile() {
        model.uploadSingleFile(view.getPath());
        view.cleanPath();
    }

    public void downloadSingleFile() {
        // TODO
    }
}
