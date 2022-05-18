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

public class ModelImpl extends Model {
    private final Presenter presenter;
    private final OkHttpClient client;
    private final Callback GETCallback = new Callback() {
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
                Log.i("response", responseBody.string());
                byte[] data = gson.fromJson(responseBody.string(), byte[].class);
                data = Compressor.decompress(data);
                Log.i("\t", Arrays.toString(data));
            } catch (IOException e) {
                Log.i("\t", e.getMessage());
            }
        }
    };
    private final Callback POSTCallback = new Callback() {
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
                Log.i("\t", responseBody.string());
            } catch (IOException e) {
                Log.i("\t", e.getMessage());
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

        String json = String.format(
                "{ \"filename\": \"%s\", \"data\": %s }",
                filename,
                Arrays.toString(Compressor.compress(data))
        );
        RequestBody requestBody = RequestBody.create(json, JSON_FORMAT);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(POSTCallback);
    }

    @Override
    public void downloadSingleFile(String filename) {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/single_file")
                .addQueryParameter("filename", filename)
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(GETCallback);
    }

    @Override
    public void readAllFiles() {
        HttpUrl url = getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("get_list_files")
                .build();

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("Read files", response.body().string());
            }
        });
    }
}
