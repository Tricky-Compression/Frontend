package ru.tricky_compression.view;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        presenter = new Presenter(this);
        findViewById(R.id.button_upload).setOnClickListener(view -> presenter.uploadSingleFile());
        findViewById(R.id.button_read).setOnClickListener(view -> presenter.readFiles());
        findViewById(R.id.button_download).setOnClickListener(view -> presenter.downloadSingleFile());
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void printInfo(String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public String getPath() {
        return ((EditText) findViewById(R.id.editText_path)).getText().toString();
    }

    @Override
    public void setPath(String text) {
        ((EditText) findViewById(R.id.editText_path)).setText(text);
    }
}
