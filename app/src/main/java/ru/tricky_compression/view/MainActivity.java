package ru.tricky_compression.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.tricky_compression.presenter.Presenter;
import ru.tricky_compression.presenter.PresenterImpl;
import ru.tricky_compression.R;

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
        Log.i("AAA", ((EditText) findViewById(R.id.editText_path)).getText().toString());
        return ((EditText) findViewById(R.id.editText_path)).getText().toString();
    }

    @Override
    public void setPath(String text) {
        ((EditText) findViewById(R.id.editText_path)).setText(text);
    }

    @Override
    public void printFileNames(String [] toDisplay) {
        runOnUiThread(() -> {

            //Init
            ListView listView = (ListView) findViewById(R.id.list);
            String[] values = toDisplay;

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values);


            // Assign adapter to ListView
            listView.setAdapter(adapter);

            // ListView Item Click Listener
            /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, android.view.View view, int i, long l) {
                    int itemPosition = i;

                    String itemValue = (String) listView.getItemAtPosition(i);

                    Toast.makeText(getApplicationContext(),
                                    "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                            .show();
                }
            }*/
        });
    }
}
