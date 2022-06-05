package ru.tricky_compression.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.entity.FileData;
import ru.tricky_compression.entity.Timestamps;

public class FileUploaderTest {
    private static final int MAX_TESTS = 10;
    private static final int CHUNK_SIZE = 4096;
    private static final Path RESOURCES = Paths.get("src", "test", "res");
    private static final Gson gson = new Gson();
    private Random random;

    private static class TrickyCallback implements Callback {
        private enum State {
            notDone, error, done
        }
        private volatile State state = State.notDone;

        public State getState() {
            return state;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            synchronized (this) {
                state = State.error;
                this.notifyAll();
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            synchronized (this) {
                if (response.isSuccessful()) {
                    state = State.done;
                    System.out.println("File was sent successfully");
                } else {
                    state = State.error;
                }
                response.close();
                this.notifyAll();
            }
        }
    }

    private String getChunk(int length) {
        var result = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            result.append((char) (random.nextInt(26) + 'a'));
        }
        return result.toString();
    }

    @BeforeEach
    public void initialize() {
        random = new Random();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void testUploadSingleFile(RepetitionInfo info) {
        try (MockWebServer server = new MockWebServer()) {
            int testNumber = info.getCurrentRepetition();
            Path testFile = RESOURCES.resolve(String.format("testUploadSingleFile%d.txt", testNumber));
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }

            String data = getChunk(random.nextInt(2 * CHUNK_SIZE + 1));
            Files.write(testFile, data.getBytes());

            server.start();
            HttpUrl url = server.url("/api/upload/single_file");
            System.out.println(url);

            TrickyCallback callback = new TrickyCallback();
            FileUploader.uploadSingleFile(url, testFile.toFile().getAbsolutePath(), callback);

            String json = server.takeRequest().getBody().readUtf8();
            FileData fileData = gson.fromJson(json, FileData.class);
            fileData.getTimestamps().setServerStart();
            Assertions.assertEquals(
                    testFile.toFile().getAbsolutePath(),
                    fileData.getFilename()
            );
            Assertions.assertArrayEquals(data.getBytes(), fileData.getData());
            fileData.getTimestamps().setServerEnd();
            json = gson.toJson(fileData.getTimestamps());
            server.enqueue(new MockResponse().setResponseCode(200).setBody(json));

            synchronized (callback) {
                while (callback.getState().equals(TrickyCallback.State.notDone)) {
                    callback.wait();
                }
            }
            if (callback.getState().equals(TrickyCallback.State.error)) {
                Assertions.fail();
            }

            Files.delete(testFile);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void testUpload(RepetitionInfo info) {
        try (MockWebServer server = new MockWebServer()) {
            int chunksNumber = info.getCurrentRepetition();
            Path testFile = RESOURCES.resolve(String.format("testUpload%d.txt", chunksNumber));
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }

            String[] chunks = new String[chunksNumber];
            for (int i = 0; i < chunksNumber; ++i) {
                chunks[i] = getChunk(i == chunksNumber - 1 ? random.nextInt(CHUNK_SIZE + 1) : CHUNK_SIZE);
            }
            Files.write(testFile, String.join("", chunks).getBytes());

            server.start();
            HttpUrl url = server.url("/api/upload/chunk");

            FileUploader.upload(url, testFile.toFile().getAbsolutePath());

            for (int i = 0; i < chunksNumber; ++i) {
                String json = server.takeRequest().getBody().readUtf8();
                ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                Timestamps timestamps = chunkData.getTimestamps();
                timestamps.setServerStart();
                Assertions.assertEquals(
                        testFile.toFile().getAbsolutePath(),
                        chunkData.getFilename()
                );
                Assertions.assertArrayEquals(
                        chunks[chunkData.getChunkNumber()].getBytes(),
                        chunkData.getData()
                );
                timestamps.setServerEnd();
                json = gson.toJson(timestamps);
                server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            }

            Files.delete(testFile);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }
}
