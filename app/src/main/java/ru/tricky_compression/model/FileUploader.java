package ru.tricky_compression.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.entity.Timestamps;

public class FileUploader {
    private static final long CHUNK_SIZE = 4096;
    private static final Gson gson = new Gson();

    private static final class UploadChunkTask implements Callable<ChunkData> {
        private final CountDownLatch latch;
        private final String filename;
        private final FileChannel channel;
        private final long chunkNumber;
        private final long offset;
        private final long size;

        public UploadChunkTask(CountDownLatch latch, String filename, FileChannel channel, long chunkNumber, long offset, long size) {
            this.latch = latch;
            this.filename = filename;
            this.channel = channel;
            this.chunkNumber = chunkNumber;
            this.offset = offset;
            this.size = size;
        }

        @Override
        public ChunkData call() throws IOException {
            ChunkData chunkData = new ChunkData(chunkNumber, filename);
            chunkData.setClientStart();
            chunkData.setData(channel.map(FileChannel.MapMode.READ_ONLY, offset, size));
            chunkData.setData(Compressor.compress(chunkData.getData()));
            latch.countDown();
            System.out.printf("Ended task %d\n", chunkNumber);
            return chunkData;
        }
    }

    private static final class TrickyCallback implements Callback {
        private final AtomicBoolean failure;

        public TrickyCallback() {
            failure = new AtomicBoolean(false);
        }

        public boolean failed() {
            return failure.get();
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            failure.set(true);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            if (failure.get()) {
                return;
            }
            if (!response.isSuccessful()) {
                failure.set(true);
                return;
            }
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    return;
                }
                Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                Log.i("chunk upload response", gson.toJson(timestamps));
            } catch (IOException ignored) {
                failure.set(true);
            }
        }
    }

    public static void upload(String filename) throws IOException {
        FileChannel channel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ).position(0);
        long fileSize = channel.size();
        int numberOfTasks = (int) ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE);
        HttpUrl url = Model.getBaseUrl()
                .addPathSegment("api")
                .addPathSegments("upload/single_file")
                .build();
        TrickyCallback callback = new TrickyCallback();
        new Thread(() -> {
            try {
                var threadPool = Executors.newCachedThreadPool();
                var latch = new CountDownLatch(numberOfTasks);

                List<Future<ChunkData>> results = new ArrayList<>(numberOfTasks);
                for (int i = 0; i < numberOfTasks; ++i) {
                    long offset = i * CHUNK_SIZE;
                    long size = Math.min(CHUNK_SIZE, fileSize - offset);
                    Callable<ChunkData> task = new UploadChunkTask(latch, filename, channel, i, offset, size);
                    results.add(threadPool.submit(task));
                }

                latch.await();

                for (var result : results) {
                    try {
                        ChunkData chunkData = result.get();
                        if (callback.failed()) {
                            threadPool.shutdownNow();
                            break;
                        }
                        Model.post(url, chunkData, callback);
                    } catch (ExecutionException ignored) {
                        threadPool.shutdownNow();
                        break;
                    }
                }

                System.out.println("All chunks are sent");
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
