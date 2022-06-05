package ru.tricky_compression.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;
import ru.tricky_compression.utils.ByteBufferUtils;

public class FileUploader {
    private static final int CHUNK_SIZE = 4096;
    private static final Gson gson = new Gson();

    private static final class UploadChunkTask implements Callable<Boolean> {
        private final HttpUrl url;
        private final String filename;
        private final int chunkNumber;
        private final long offset;
        private final int size;
        private final TrickyCallback callback;

        public UploadChunkTask(
                HttpUrl url,
                String filename,
                int chunkNumber,
                long offset,
                int size,
                TrickyCallback callback) {
            this.url = url;
            this.filename = filename;
            this.chunkNumber = chunkNumber;
            this.offset = offset;
            this.size = size;
            this.callback = callback;
        }

        @Override
        public Boolean call() {
            try (FileChannel channel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ)) {
                ChunkData chunkData = new ChunkData(chunkNumber, filename);
                chunkData.getTimestamps().setClientStart();
                var buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, size);
                chunkData.setData(ByteBufferUtils.toByteArray(buffer, size));
//                chunkData.setData(Compressor.compress(chunkData.getData()));
                if (callback.failed()) {
                    return false;
                }
                Model.post(url, chunkData, callback);
                System.out.printf("Chunk #%d is sent\n", chunkNumber);
                System.out.flush();
                return true;
            } catch (IOException ignored) {
                return false;
            }
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
            failure.compareAndSet(false, true);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            if (failed()) {
                return;
            }
            if (!response.isSuccessful()) {
                failure.compareAndSet(false, true);
                return;
            }
            try (ResponseBody responseBody = response.body()) {
                Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                timestamps.setClientEnd();
                Log.i("chunk upload response", gson.toJson(timestamps));
            } catch (IOException ignored) {
                failure.compareAndSet(false, true);
            }
        }
    }

    public static void uploadSingleFile(HttpUrl url, String filename, Callback callback) throws IOException {
        FileData fileData = new FileData(filename);
        fileData.getTimestamps().setClientStart();
        fileData.setData(Files.readAllBytes(Paths.get(filename)));
//        fileData.setData(Compressor.compress(fileData.getData()));
        Model.post(url, fileData, callback);
    }

    public static void upload(HttpUrl url, String filename) throws IOException {
        long fileSize = Files.size(Paths.get(filename));
        int numberOfTasks = (int) ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE);
        TrickyCallback callback = new TrickyCallback();
        new Thread(() -> {
            try {
                var threadPool = Executors.newCachedThreadPool();

                List<Future<Boolean>> results = new ArrayList<>(numberOfTasks);
                for (int i = 0; i < numberOfTasks; ++i) {
                    long offset = (long) i * CHUNK_SIZE;
                    int size = (int) Math.min(CHUNK_SIZE, fileSize - offset);
                    Callable<Boolean> task = new UploadChunkTask(url, filename, i, offset, size, callback);
                    results.add(threadPool.submit(task));
                }

                boolean fail = false;
                for (var result : results) {
                    try {
                        if (!result.get()) {
                            fail = true;
                            break;
                        }
                    } catch (ExecutionException ignored) {
                        fail = true;
                        break;
                    }
                }

                if (fail) {
                    threadPool.shutdownNow();
                    Log.i("chunk upload", "uploading failed");
                } else {
                    System.out.printf("%d chunks are sent\n", numberOfTasks);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
