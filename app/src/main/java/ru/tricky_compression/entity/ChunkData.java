package ru.tricky_compression.entity;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class ChunkData extends Timestamps {
    private long chunkNumber;
    private String filename;
    private ByteBuffer data;

    public ChunkData() {
    }

    public ChunkData(long chunkNumber, String filename) {
        this.chunkNumber = chunkNumber;
        this.filename = filename;
    }

    public long getChunkNumber() {
        return chunkNumber;
    }

    public String getFilename() {
        return filename;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setChunkNumber(long chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
