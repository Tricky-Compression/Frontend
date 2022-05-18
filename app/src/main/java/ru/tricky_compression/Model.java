package ru.tricky_compression;

import com.google.gson.Gson;

import okhttp3.HttpUrl;
import okhttp3.MediaType;

public abstract class Model {
    protected static final String IP = "51.250.23.237";
    protected static final int PORT = 1337;
    protected static final MediaType JSON_FORMAT = MediaType.parse("application/json; charset=utf-8");
    protected static final Gson gson = new Gson();

    public static HttpUrl.Builder getBaseUrl() {
        return new HttpUrl.Builder().scheme("http").host(IP).port(PORT);
    }

    public abstract void uploadSingleFile(String filename);

    public abstract void downloadSingleFile(String filename);
}
