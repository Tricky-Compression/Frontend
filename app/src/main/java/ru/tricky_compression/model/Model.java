package ru.tricky_compression.model;

import com.google.gson.Gson;

import okhttp3.HttpUrl;
import okhttp3.MediaType;

public interface Model {
    String IP = "51.250.23.237";
    int PORT = 1337;
    MediaType JSON_FORMAT = MediaType.parse("application/json; charset=utf-8");
    Gson gson = new Gson();

    static HttpUrl.Builder getBaseUrl() {
        return new HttpUrl.Builder().scheme("http").host(IP).port(PORT);
    }

    void uploadSingleFile(String filename);

    void downloadSingleFile(String filename);

    void readAllFiles();
}
