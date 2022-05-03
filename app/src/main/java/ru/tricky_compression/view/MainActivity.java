package ru.tricky_compression.view;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import ru.tricky_compression.Model;
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
        Model model = new ModelImpl();
        presenter = new Presenter(model);
        presenter.attachView(this);
        findViewById(R.id.button_greeting).setOnClickListener(view -> presenter.sendGreeting());
        findViewById(R.id.button_showInfo).setOnClickListener(view -> presenter.uploadSingleFile());
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public String getText() {
        EditText editText = findViewById(R.id.editText_path);
        return editText.getText().toString();
    }

    @Override
    public void setText(String text) {
        EditText editText = findViewById(R.id.editText_path);
        editText.setText(text);
    }
}
