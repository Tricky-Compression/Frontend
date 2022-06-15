package ru.tricky_compression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public interface Common {
    int MAX_FILE_SIZE = 1000000;
    Path RESOURCES = Paths.get("src", "test", "res");
    Random random = new Random();

    static Path getFilename(int testNumber) {
        String baseFilename = Thread.currentThread().getStackTrace()[2].getMethodName();
        return RESOURCES.resolve(String.format("%s%d.txt", baseFilename, testNumber));
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
}
