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
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;

public class ModelImpl extends Model {
    private final Presenter presenter;
    private final OkHttpClient client;
    private final Callback onUpload = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            presenter.printInfo(e.getMessage());
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return;
                }
                Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                timestamps.setClientEnd();
                Log.i("upload response", gson.toJson(timestamps));
            } catch (IOException e) {
                Log.i("upload response error", e.getMessage());
            }
        }
    };
    private final Callback onDownload = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            presenter.printInfo(e.getMessage());
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return;
                }
                FileData fileData = gson.fromJson(responseBody.string(), FileData.class);
                fileData.setData(Compressor.decompress(fileData.getData()));
                fileData.getTimestamps().setClientEnd();
                Log.i("download response", gson.toJson(fileData));
            } catch (IOException e) {
                Log.i("download response error", e.getMessage());
            }
        }
    };

    public ModelImpl(Presenter presenter) {
        this.presenter = presenter;
        client = new OkHttpClient();
        ping();
        System.out.println(" ----- Model was created ----- ");
    }

    public void ping() {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("greeting")
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                presenter.printInfo(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                presenter.printInfo(String.valueOf(response.code()));
            }
        });
    }

    @Override
    public void uploadSingleFile(String filename) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty filename");
            return;
        }

        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/single_file")
                .build();

        FileData fileData = new FileData(filename);
        fileData.getTimestamps().setClientStart();
        try {
            fileData.setData(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            presenter.printInfo(e.getMessage());
            return;
        }
        fileData.setData(new byte[]{1, 2, 3, 4});
        fileData.setData(Compressor.compress(fileData.getData()));
        RequestBody requestBody = RequestBody.create(gson.toJson(fileData), JSON_FORMAT);
        Log.i("request", gson.toJson(fileData));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(onUpload);
    }

    @Override
    public void downloadSingleFile(String filename) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty filename");
            return;
        }

        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/single_file")
                .addQueryParameter("filename", filename)
                .addQueryParameter("clientStart", String.valueOf(System.nanoTime()))
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(onDownload);
    }

    @Override
    public void readAllFiles() {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegment("get_list_files")
                .build();

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String[] display = response.body().string().split(":");
                Log.i("TEST", Arrays.toString(display));
                presenter.passFileNames(display);
            }
        });
    }
}
