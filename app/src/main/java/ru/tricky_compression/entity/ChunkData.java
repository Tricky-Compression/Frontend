package ru.tricky_compression.entity;

@SuppressWarnings("unused")
public class ChunkData {
    private final Timestamps timestamps = new Timestamps();
    private int chunkNumber;
    private String filename;
    private byte[] data;

    public ChunkData() {
    }

    public ChunkData(int chunkNumber, String filename) {
        this.chunkNumber = chunkNumber;
        this.filename = filename;
    }

    public final Timestamps getTimestamps() {
        return timestamps;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
