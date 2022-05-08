package ru.tricky_compression.model;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.Model;
import ru.tricky_compression.Presenter;

public final class ModelImpl extends Model {
    private final OkHttpClient client;
    protected final Callback callback = new Callback() {
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
            presenter.printInfo(e.getMessage());
        }
    };
    private final Presenter presenter;

    public ModelImpl(Presenter presenter) {
        client = new OkHttpClient();
        this.presenter = presenter;
        System.out.println(" ----- Model was created ----- ");
    }

    @Override
    public void sendGreeting() {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("greeting")
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    @Override
    public void uploadSingleFile(String filename) {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/single_file")
                .build();

        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            presenter.printInfo(e.getMessage());
            return;
        }

        String json = String.format("{ \"filename\": \"%s\", \"data\": %s }", filename, Arrays.toString(data));
        RequestBody requestBody = RequestBody.create(json, JSON_FORMAT);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

    @Override
    public void downloadSingleFile(String filename) {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/single_file")
                .addQueryParameter("filename", filename)
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
}
