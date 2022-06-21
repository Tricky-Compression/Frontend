package ru.tricky_compression;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.stream.Stream;

import ru.tricky_compression.entity.ChunkData;

public interface Common {
    int MAX_FILE_SIZE = 1000000;
    Gson gson = new Gson();
    Path RESOURCES = Paths.get("src", "test", "res");
    Random random = new Random();

    static void cleanUpTimestamps() throws IOException {
        Path timestamps = RESOURCES.resolve("timestamps.txt");
        if (Files.exists(timestamps)) {
            Files.delete(timestamps);
        }
        Files.createFile(timestamps);
    }

    static Path getFilename(Integer... args) {
        String baseFilename = Thread.currentThread().getStackTrace()[2].getMethodName();

        var builder = new StringBuilder(baseFilename);
        Stream.of(args).forEach(i -> builder.append('_').append(i));
        builder.append(".txt");
        return RESOURCES.resolve(builder.toString());
    }

    static String getChunk(int length) {
        var result = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            result.append((char) (random.nextInt(26) + 'a'));
        }
        return result.toString();
    }

    static String fillFile(Path testFile, int fileSize) throws IOException {
        String chunk = getChunk(fileSize);
        Files.write(testFile, chunk.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        return chunk;
    }

    static String[] fillFile(Path testFile, int chunksNumber, int chunkSize) throws IOException {
        String[] chunks = new String[chunksNumber];
        for (int i = 0; i < chunksNumber; ++i) {
            if (i == chunksNumber - 1) {
                chunks[i] = getChunk(random.nextInt(chunkSize + 1));
            } else {
                chunks[i] = getChunk(chunkSize);
            }
        }
        Files.write(testFile, String.join("", chunks).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        return chunks;
    }

    static String emulateServer(String input, Path path) throws IOException {
        ChunkData chunkData = gson.fromJson(input, ChunkData.class);
        chunkData.getTimestamps().setServerStart();

        Files.write(
                path,
                chunkData.getData(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        Files.delete(path);

        chunkData.getTimestamps().setServerEnd();
        return gson.toJson(chunkData.getTimestamps());
    }
}
