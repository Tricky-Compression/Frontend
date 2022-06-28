package ru.tricky_compression.model;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;
import ru.tricky_compression.presenter.Presenter;

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
                presenter.printNetworkError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                presenter.printInfo(String.valueOf(response.code()));
            }
        });
    }

    public void upload(String filename) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty file name");
            return;
        }

        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/chunk")
                .build();
        try {
            fileuploader.upload(url, filename);
        } catch (IOException e) {
            presenter.printInfo(String.format("can't upload file %s", filename));
        }
    }

    @Override
    public void uploadSingleFile(String filename) {
        if (filename.isEmpty()) {
            presenter.printInfo("Empty file name");
            return;
        }

        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/single_file")
                .build();
        try {
            FileUploader.uploadSingleFile(url, filename, new Callback() {
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
                        Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                        timestamps.setClientEnd();
                        Log.i("upload response", gson.toJson(timestamps));
                    } catch (IOException e) {
                        Log.e("upload response", e.toString());
                    }
                }
            });
        } catch (IOException e) {
            presenter.printInfo(String.format("can't upload file %s", filename));
        }
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
                    FileData fileData = gson.fromJson(responseBody.string(), FileData.class);
                    fileData.setData(Compressor.decompress(fileData.getData()));
                    fileData.getTimestamps().setClientEnd();
                    Log.i("download response", gson.toJson(fileData));
                } catch (IOException e) {
                    Log.e("download response", e.toString());
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
                .addPathSegments("download/chunk")
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
