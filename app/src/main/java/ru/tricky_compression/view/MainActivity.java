package ru.tricky_compression.view;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import ru.tricky_compression.model.ModelImpl;
import ru.tricky_compression.Presenter;
import ru.tricky_compression.R;
import ru.tricky_compression.View;

public class MainActivity extends AppCompatActivity implements View {

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        init();
    }

    private void init() {
        presenter = new Presenter(new ModelImpl(), this);
        findViewById(R.id.button_greeting).setOnClickListener(view -> presenter.sendGreeting());
        findViewById(R.id.button_uploadFile).setOnClickListener(view -> presenter.uploadSingleFile());
        findViewById(R.id.button_downloadFile).setOnClickListener(view -> presenter.downloadSingleFile());
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public String getPath() {
        EditText editText = findViewById(R.id.editText_path);
        return editText.getText().toString();
    }

    @Override
    public void setPath(String text) {
        EditText editText = findViewById(R.id.editText_path);
        editText.setText(text);
    }
}
