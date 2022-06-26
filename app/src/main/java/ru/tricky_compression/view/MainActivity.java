package ru.tricky_compression.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import ru.tricky_compression.R;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.presenter.Presenter;
import ru.tricky_compression.presenter.PresenterImpl;

public class MainActivity extends AppCompatActivity implements View {

    private Presenter presenter;
    private ViewFlipper viewFlipper;
    private ArrayList<String> chunksCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        init();
    }

    private void init() {
        chunksCache = new ArrayList<>();
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

                Intent readIntent = new Intent(this, ReadActivity.class);
                readIntent.putExtra("fileName", itemValue);
                startActivity(readIntent);
            });
        });
    }


    public void previousView(android.view.View v){
        viewFlipper.showPrevious();
    }

    public void nextView(android.view.View view) {
        viewFlipper.showNext();
    }
}
