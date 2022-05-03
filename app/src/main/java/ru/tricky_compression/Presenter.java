package ru.tricky_compression;

public class Presenter {
    private final Model model;
    private View view;

    public Presenter(Model model) {
        this.model = model;
        System.out.println(" ----- Presenter was created ----- ");
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }

    public void sendGreeting() {
        model.sendGreeting();
    }

    public void uploadSingleFile() {
        model.uploadSingleFile(view.getText());
        view.setText("");
    }
}
