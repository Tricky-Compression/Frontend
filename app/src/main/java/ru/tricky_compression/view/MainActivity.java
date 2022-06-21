package ru.tricky_compression.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import ru.tricky_compression.R;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.presenter.Presenter;
import ru.tricky_compression.presenter.PresenterImpl;

public class MainActivity extends AppCompatActivity implements View {

    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        init();
    }

    private void init() {
        presenter = new PresenterImpl(this);
        findViewById(R.id.button_upload).setOnClickListener(view -> presenter.uploadSingleFile());
        findViewById(R.id.button_read).setOnClickListener(view -> presenter.readFilenames());
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

    @Override
    public void printFileNames(String [] toDisplay) {
        runOnUiThread(() -> {
            ListView listView = findViewById(R.id.list);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, toDisplay);

            listView.setAdapter(adapter);

            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                String itemValue = (String) listView.getItemAtPosition(i);
                presenter.sendChunkDownloadRequest(itemValue, 1);
            });
        });
    }

    @Override
    public void showChunk(ChunkData chunkData) {
        runOnUiThread(() -> {
            setContentView(R.layout.read_screen);
            EditText readScreen = findViewById(R.id.text_read_chunk);
            readScreen.setText(Arrays.toString(chunkData.getData()));
        });
    }
}
