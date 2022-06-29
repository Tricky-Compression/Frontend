package ru.tricky_compression.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ru.tricky_compression.R;
import ru.tricky_compression.utils.FileChooserUtils;

public class FileChooserFragment extends Fragment {

    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_RESULT_CODE_FILE_CHOOSER = 2000;

    private EditText editTextPath;

    private static final String LOG_TAG = "AndroidExample";

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_file_chooser, container, false);

        this.editTextPath = rootView.findViewById(R.id.editText_path);
        Button buttonBrowse = rootView.findViewById(R.id.button_browse);

        buttonBrowse.setOnClickListener(view -> askPermissionAndBrowseFile());
        return rootView;
    }

    private void askPermissionAndBrowseFile()  {
        int permission = ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_REQUEST_CODE_PERMISSION
            );
            return;
        }
        this.doBrowseFile();
    }

    private void doBrowseFile()  {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");

        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILE_CHOOSER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(LOG_TAG, "Permission granted!");
                Toast.makeText(this.getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();

                this.doBrowseFile();
            } else {
                Log.i(LOG_TAG, "Permission denied!");
                Toast.makeText(this.getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_RESULT_CODE_FILE_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri fileUri = data.getData();
                    Log.i(LOG_TAG, "Uri: " + fileUri);

                    String filePath = null;
                    try {
                        filePath = FileChooserUtils.getPath(this.getContext(), fileUri);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error: " + e);
                        Toast.makeText(this.getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                    }
                    this.editTextPath.setText(filePath);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}