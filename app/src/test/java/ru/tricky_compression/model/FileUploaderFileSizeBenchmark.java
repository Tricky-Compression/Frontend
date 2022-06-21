package ru.tricky_compression.model;

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
import ru.tricky_compression.Common;

public class FileUploaderFileSizeBenchmark {
    private static final int MAX_TESTS = 10;
    private static FileUploader fileUploader;

    @BeforeAll
    public static void setUp() {
        try {
            Common.cleanUpTimestamps();
        } catch (IOException ignored) {
            Assertions.fail();
        }
        fileUploader = new FileUploader();
    }

    @Execution(ExecutionMode.CONCURRENT)
    @RepeatedTest(MAX_TESTS)
    public void measureUploadWithDifferentFileSize(RepetitionInfo info) {
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
                Path path = Common.getFilename(info.getCurrentRepetition(), i);
                json = Common.emulateServer(json, path);
                server.enqueue(new MockResponse().setResponseCode(200).setBody(json));
            }

            Files.delete(testFile);
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (IOException | InterruptedException ignored) {
            Assertions.fail();
        }
    }
}
