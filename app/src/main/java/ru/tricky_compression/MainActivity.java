package ru.tricky_compression;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button uploadButton = findViewById(R.id.btn_upload);
        uploadButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MyAwesomeActivity.class);
            startActivity(intent);
        });
    }
}