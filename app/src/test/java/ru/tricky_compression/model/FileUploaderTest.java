package ru.tricky_compression.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import ru.tricky_compression.Common;
import ru.tricky_compression.entity.ChunkData;
import ru.tricky_compression.entity.FileData;

public class FileUploaderTest {
    private static final int MAX_TESTS = 10;
    private static final Gson gson = new Gson();

    private static class TrickyCallback implements Callback {
        private enum State {
            notDone, error, done
        }
        private volatile State state = State.notDone;

        public State getState() {
            return state;
        }

        public synchronized void await() throws InterruptedException {
            while (state.equals(State.notDone)) {
                this.wait();
            }
        }

        @Override
        public synchronized void onFailure(@NonNull Call call, @NonNull IOException e) {
            state = State.error;
            this.notifyAll();
        }

        @Override
        public synchronized void onResponse(@NonNull Call call, @NonNull Response response) {
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

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void testUploadSingleFile(RepetitionInfo info) {
        try (MockWebServer server = new MockWebServer()) {
            Path testFile = Common.getFilename(info.getCurrentRepetition());
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }

            var chunk = Common.fillFile(testFile, Common.random.nextInt(Common.MAX_FILE_SIZE));

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
            Assertions.assertArrayEquals(chunk.getBytes(), fileData.getData());
            fileData.getTimestamps().setServerEnd();
            json = gson.toJson(fileData.getTimestamps());
            server.enqueue(new MockResponse().setResponseCode(200).setBody(json));

            callback.await();
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
        FileUploader fileUploader = new FileUploader();
        try (MockWebServer server = new MockWebServer()) {
            Path testFile = Common.getFilename(info.getCurrentRepetition());
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }

            int chunksNumber = info.getCurrentRepetition();
            var chunks = Common.fillFile(testFile, chunksNumber, fileUploader.getChunkSize());

            server.start();
            HttpUrl url = server.url("/api/upload/chunk");

            fileUploader.upload(url, testFile.toFile().getAbsolutePath());

            for (int i = 0; i < chunksNumber; ++i) {
                String json = server.takeRequest().getBody().readUtf8();
                ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                chunkData.getTimestamps().setServerStart();
                Assertions.assertEquals(
                        testFile.toFile().getAbsolutePath(),
                        chunkData.getFilename()
                );
                Assertions.assertArrayEquals(
                        chunks[chunkData.getChunkNumber()].getBytes(),
                        chunkData.getData()
                );
                chunkData.getTimestamps().setServerEnd();
                json = gson.toJson(chunkData.getTimestamps());
                server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            }

            Files.delete(testFile);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }
}
