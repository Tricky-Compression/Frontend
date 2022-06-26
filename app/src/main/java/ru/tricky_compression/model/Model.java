package ru.tricky_compression.model;

import android.util.Log;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public interface Model {
    String IP = "51.250.23.237";
    int PORT = 1337;
    MediaType JSON_FORMAT = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    Gson gson = new Gson();

    static HttpUrl.Builder getBaseUrl() {
        return new HttpUrl.Builder().scheme("http").host(IP).port(PORT);
    }

    static void get(HttpUrl url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    static Call blockingGet(HttpUrl url) {
        Request request = new Request.Builder().url(url).build();
        return client.newCall(request);
    }

    static void post(HttpUrl url, Object requestData, Callback callback) {
        RequestBody requestBody = RequestBody.create(gson.toJson(requestData), JSON_FORMAT);
        Log.i("request", gson.toJson(requestData));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

    static Call blockingPost(HttpUrl url, Object requestData) {
        RequestBody requestBody = RequestBody.create(gson.toJson(requestData), JSON_FORMAT);
        Log.i("request", gson.toJson(requestData));
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return client.newCall(request);
    }

    void uploadSingleFile(String filename);

    void downloadSingleFile(String filename);

    void downloadChunk(String filename, int number);

    void readAllFiles();
}