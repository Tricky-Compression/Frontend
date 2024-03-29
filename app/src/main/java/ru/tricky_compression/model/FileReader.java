package ru.tricky_compression.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.view.ReadActivity;

public class FileReader {
    private static final Gson gson = new Gson();
    private static final int LEFT_CACHE = 1;
    private static final int RIGHT_CACHE = 3;
    private final String filename;
    private int CUR_CHUNK = 0;
    private final Map<Integer, String> chunksCache;
    private ReadActivity readActivity;

    public FileReader(ReadActivity readActivity, String filename) {
        this.filename = filename;
        chunksCache = new ConcurrentHashMap<>();
        this.readActivity = readActivity;
    }

    public void onDestroy() {
        readActivity = null;
    }

    private void pullChunk(int chunkNumber) {
        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/chunk")
                .addQueryParameter("number", String.valueOf(chunkNumber))
                .addQueryParameter("filename", filename)
                .addQueryParameter("clientStart", String.valueOf(System.nanoTime()))
                .build();

        Model.get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                readActivity.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    response.close();
                    return;
                }
                try (ResponseBody responseBody = response.body()) {
                    String json = responseBody.string();
                    System.out.println(json);
                    ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                    String converted = new String(chunkData.getData(), StandardCharsets.UTF_8);
                    chunksCache.put(chunkData.getChunkNumber(), converted);
                } catch (IOException e) {
                    Log.e("download chunk response", e.toString());
                }
            }
        });
    }

    public void pullChunks() {
        if (!chunksCache.containsKey(CUR_CHUNK)) {
            pullChunk(CUR_CHUNK);
        }
        for (int i = CUR_CHUNK + 1; i <= CUR_CHUNK + RIGHT_CACHE; ++i) {
            if (!chunksCache.containsKey(i)) {
                pullChunk(i);
            }
        }
        for (int i = CUR_CHUNK - 1; i >= Math.max(CUR_CHUNK - LEFT_CACHE, 0); --i) {
            if (!chunksCache.containsKey(i)) {
                pullChunk(i);
            }
        }
    }

    public String getCurrentChunk(){
        return chunksCache.get(CUR_CHUNK - 1);
    }

    public void goRight(){
        CUR_CHUNK++;
    }

    public void goLeft(){
        CUR_CHUNK--;
    }

    public int getChunkNumber(){
        return CUR_CHUNK;
    }
}
