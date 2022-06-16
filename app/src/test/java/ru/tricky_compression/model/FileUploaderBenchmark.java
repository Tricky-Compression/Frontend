package ru.tricky_compression.model;

import com.google.gson.Gson;

import ru.tricky_compression.Common;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import ru.tricky_compression.entity.ChunkData;

public class FileUploaderBenchmark {
    private static final int MAX_TESTS = 10;
    private static final Gson gson = new Gson();
    private static Path testFile;

    @BeforeAll
    public static void initialize() {
        try {
            testFile = Common.getFilename(0);
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }
            Common.fillFile(testFile, Common.MAX_FILE_SIZE);
        } catch (IOException ignored) {
            Assertions.fail();
        }
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void measureUploadWithDifferentFileSize(RepetitionInfo info) {
        var fileUploader = new FileUploader();
        try (var server = new MockWebServer()) {
            Path testFile = Common.getFilename(info.getCurrentRepetition());
            if (!Files.exists(testFile)) {
                Files.createFile(testFile);
            }

            int chunksNumber = info.getCurrentRepetition();
            Common.fillFile(testFile, chunksNumber, fileUploader.getChunkSize());

            server.start();
            HttpUrl url = server.url("/api/upload/chunk");

            fileUploader.upload(url, testFile.toFile().getAbsolutePath());

            for (int i = 0; i < chunksNumber; ++i) {
                String json = server.takeRequest().getBody().readUtf8();
                ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                chunkData.getTimestamps().setServerStart();
                chunkData.getTimestamps().setServerEnd();
                json = gson.toJson(chunkData.getTimestamps());
                server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            }

            Files.delete(testFile);
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void measureUploadWithDifferentChunkSize(RepetitionInfo info) {
        var fileUploader = new FileUploader(1536 * info.getCurrentRepetition());
        try (var server = new MockWebServer()) {
            server.start();
            HttpUrl url = server.url("/api/upload/chunk");

            fileUploader.upload(url, testFile.toFile().getAbsolutePath());

            int chunksNumber = fileUploader.getNumberOfTasks(Common.MAX_FILE_SIZE);
            for (int i = 0; i < chunksNumber; ++i) {
                String json = server.takeRequest().getBody().readUtf8();
                ChunkData chunkData = gson.fromJson(json, ChunkData.class);
                chunkData.getTimestamps().setServerStart();
                chunkData.getTimestamps().setServerEnd();
                json = gson.toJson(chunkData.getTimestamps());
                server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            }

            TimeUnit.MILLISECONDS.sleep(100);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }

    @AfterAll
    public static void cleanUp() {
        try {
            Files.delete(testFile);
        } catch (IOException e) {
            Assertions.fail();
        }
    }
}
