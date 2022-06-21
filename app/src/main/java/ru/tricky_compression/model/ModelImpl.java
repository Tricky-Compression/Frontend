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
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.presenter.Presenter;
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;

public class ModelImpl implements Model {
    private final Presenter presenter;

    public ModelImpl(Presenter presenter) {
        this.presenter = presenter;
        ping();
        System.out.println(" ----- Model was created ----- ");
    }

    public void ping() {
        HttpUrl url = Model.getBaseUrl()
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

        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/single_file")
                .build();

        FileData fileData = new FileData(filename);
        fileData.getTimestamps().setClientStart();
        try {
            fileData.setData(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException ignored) {
            presenter.printInfo(String.format("can't upload file %s", filename));
            return;
        }
        fileData.setData(new byte[]{1, 2, 3, 4});
        fileData.setData(Compressor.compress(fileData.getData()));

        Model.post(url, fileData, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                presenter.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    presenter.printInfo(String.valueOf(response.code()));
                    return;
                }
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        return;
                    }
                    Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                    timestamps.setClientEnd();
                    Log.i("upload response", gson.toJson(timestamps));
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public void downloadSingleFile(String filename) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty filename");
            return;
        }

        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/single_file")
                .addQueryParameter("filename", filename)
                .addQueryParameter("clientStart", String.valueOf(System.nanoTime()))
                .build();

        Model.get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                presenter.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    presenter.printInfo(String.valueOf(response.code()));
                    return;
                }
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        return;
                    }
                    FileData fileData = gson.fromJson(responseBody.string(), FileData.class);
                    fileData.setData(Compressor.decompress(fileData.getData()));
                    fileData.getTimestamps().setClientEnd();
                    Log.i("download response", gson.toJson(fileData));
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public void downloadChunk(String filename, int number) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty filename");
            return;
        }

        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("download/single_file")
                .addQueryParameter("number", String.valueOf(number))
                .addQueryParameter("filename", filename)
                .addQueryParameter("clientStart", String.valueOf(System.nanoTime()))
                .build();

        Model.get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                presenter.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    presenter.printInfo(String.valueOf(response.code()));
                    response.close();
                    return;
                }
                try (ResponseBody responseBody = response.body()) {
                    String json = responseBody.string();
                    System.out.println(json);
                    ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                    presenter.afterReceivingChunk(chunkData);
                } catch (IOException e) {
                    Log.e("download chunk response", e.toString());
                }
            }
        });
    }

    @Override
    public void readAllFiles() {
        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegment("get_list_files")
                .build();

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                presenter.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        presenter.printInfo(String.valueOf(response.code()));
                        System.out.println(response.body().string());
                        return;
                    }
                    String json = responseBody.string();
                    String[] display = gson.fromJson(json, String[].class);
                    Log.i("readAllFiles response", json);
                    presenter.writeFilenames(display);
                } catch (IOException e) {
                    Log.e("readAllFiles response", e.toString());
                }
            }
        });
    }
}
