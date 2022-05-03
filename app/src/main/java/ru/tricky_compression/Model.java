package ru.tricky_compression;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class Model {
    protected static final String IP = "51.250.108.34";
    protected static final int PORT = 1337;
    protected static final String BASE_URL = String.format("http://%s:%d", IP, PORT);
    protected static final MediaType JSON_FORMAT = MediaType.parse("application/json; charset=utf-8");
    protected static final Callback callback = new Callback() {
        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            ResponseBody responseBody = response.body();
            try {
                if (responseBody != null) {
                    Log.i("\t", responseBody.string());
                }
            } catch (IOException e) {
                Log.i("\t", e.getMessage());
            }
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.i("\t", e.getMessage());
        }
    };

    protected abstract void sendGreeting();

    protected abstract void uploadSingleFile(String filename);
}
