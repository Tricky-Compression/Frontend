package ru.tricky_compression.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import ru.tricky_compression.R;
import ru.tricky_compression.model.FileReader;

public class ReadActivity extends AppCompatActivity implements View {
    private ViewFlipper viewFlipper;
    private FileReader fileReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fileName = this.getIntent().getStringExtra("fileName");
        fileReader = new FileReader(this, fileName);

        this.setContentView(R.layout.chunk_pages);
        viewFlipper = findViewById(R.id.chunk_flipper);
        fileReader.pullChunks();
    }

    @Override
    protected void onDestroy() {
        fileReader.onDestroy();
        super.onDestroy();
    }

    @Override
    public void printInfo(String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String text) {

    }

    @Override
    public void printFileNames(String[] toDisplay) {
    }

    public void showChunk() {
        TextView textView = new TextView(this);
        String display = fileReader.getCurrentChunk();
        textView.setText(display);
        textView.setTextSize(30);
        viewFlipper.addView(textView);
    }


    public void previousView(android.view.View v) {
        viewFlipper.showPrevious();
    }

    public void nextView(android.view.View view) {
        showChunk();
        viewFlipper.showNext();
    }


}
