package ru.tricky_compression.model;

import android.util.Log;

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

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;
import ru.tricky_compression.utils.ByteBufferUtils;

public class FileUploader {
    private static final int DEFAULT_CHUNK_SIZE = 4096;
    private final int chunkSize;
    private static final Gson gson = new Gson();

    public FileUploader() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public FileUploader(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getNumberOfTasks(long fileSize) {
        return (int) ((fileSize + chunkSize - 1) / chunkSize);
    }

    private static final class UploadChunkTask implements Callable<Boolean> {
        private final HttpUrl url;
        private final String filename;
        private final int chunkNumber;
        private final long offset;
        private final int size;

        public UploadChunkTask(
                HttpUrl url,
                String filename,
                int chunkNumber,
                long offset,
                int size) {
            this.url = url;
            this.filename = filename;
            this.chunkNumber = chunkNumber;
            this.offset = offset;
            this.size = size;
        }

        @Override
        public Boolean call() {
            ChunkData chunkData = new ChunkData(chunkNumber, filename);
            chunkData.getTimestamps().setClientStart();
            try (var channel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ)) {
                var buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, size);
                chunkData.setData(ByteBufferUtils.toByteArray(buffer, size));
//                chunkData.setData(Compressor.compress(chunkData.getData()));
            } catch (IOException ignored) {
                return false;
            }
            try (Response response = Model.nonBlockingPost(url, chunkData).execute();
                 ResponseBody responseBody = response.body()) {
                Timestamps timestamps = gson.fromJson(responseBody.string(), Timestamps.class);
                timestamps.setClientEnd();
                /*System.out.println(gson.toJson(timestamps));
                System.out.printf("Chunk #%d is sent\n", chunkNumber);
                System.out.flush();*/
                return true;
            } catch (IOException ignored) {
                return false;
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

    public void upload(HttpUrl url, String filename) throws IOException {
        long fileSize = Files.size(Paths.get(filename));
        int numberOfTasks = getNumberOfTasks(fileSize);
        new Thread(() -> {
            try {
                var threadPool = Executors.newCachedThreadPool();

                List<Future<Boolean>> futures = new ArrayList<>(numberOfTasks);
                for (int i = 0; i < numberOfTasks; ++i) {
                    long offset = (long) i * chunkSize;
                    int size = (int) Math.min(chunkSize, fileSize - offset);
                    Callable<Boolean> task = new UploadChunkTask(url, filename, i, offset, size);
                    futures.add(threadPool.submit(task));
                }

                boolean fail = false;
                try {
                    for (var future : futures) {
                        if (!future.get()) {
                            fail = true;
                            break;
                        }
                    }
                } catch (ExecutionException ignored) {
                    fail = true;
                }

                if (fail) {
                    threadPool.shutdownNow();
                    Log.i("chunk upload", "uploading failed");
                } else {
                    /*try (var fw = new FileWriter(Paths.get("src/test/res/timestamps.txt").toFile(), true)) {
                        fw.write(totalTime + "\n");
                    } catch (IOException ignored) {
                    }
                    System.out.printf("total time %d ns", totalTime);*/
                    System.out.printf("%d chunks are sent\n", numberOfTasks);
                    System.out.flush();
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }
}
