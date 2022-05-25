package ru.tricky_compression.entity;

@SuppressWarnings("unused")
public class FileData {
    private final Timestamps timestamps = new Timestamps();
    private String filename;
    private byte[] data;

    public FileData() {
    }

    public FileData(String filename) {
        this.filename = filename;
    }

    public final Timestamps getTimestamps() {
        return timestamps;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
