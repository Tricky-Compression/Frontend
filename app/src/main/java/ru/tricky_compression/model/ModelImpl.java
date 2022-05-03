package ru.tricky_compression.model;

import android.util.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import ru.tricky_compression.Model;

public final class ModelImpl extends Model {
    private final OkHttpClient client;

    public ModelImpl() {
        client = new OkHttpClient();
        System.out.println(" ----- Model was created ----- ");
    }

    @Override
    public void sendGreeting() {
        String url = String.format("%s/api/greeting", BASE_URL);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    @Override
    public void uploadSingleFile(String filename) {
        String url = String.format("%s/api/upload/single_file", BASE_URL);
        String data;
        try {
            data = Arrays.toString(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            Log.i("\t", String.format("No file called \"%s\"", filename));
            return;
        }
        String json = String.format("{ \"filename\": \"%s\", \"data\": \"%s\" }", filename, data);
        RequestBody requestBody = RequestBody.create(json, JSON_FORMAT);
        Log.i("\t", json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}
