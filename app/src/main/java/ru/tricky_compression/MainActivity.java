package ru.tricky_compression;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private FileChooserFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragment_fileChooser);

        Button buttonShowInfo = this.findViewById(R.id.button_showInfo);

        buttonShowInfo.setOnClickListener(view -> showInfo());
    }

    private void showInfo()  {
        String path = this.fragment.getPath();
        Toast.makeText(this, "Path: " + path, Toast.LENGTH_LONG).show();
    }
}
