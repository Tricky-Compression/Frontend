package ru.tricky_compression.entity;

@SuppressWarnings("unused")
public class Timestamps {
    private long clientStart;
    private long clientEnd;
    private long serverStart;
    private long serverEnd;

    public long getClientStart() {
        return clientStart;
    }

    public long getClientEnd() {
        return clientEnd;
    }

    public long getServerStart() {
        return serverStart;
    }

    public long getServerEnd() {
        return serverEnd;
    }

    public void setClientStart() {
        clientStart = System.nanoTime();
    }

    public void setClientEnd() {
        clientEnd = System.nanoTime();
    }

    public void setServerStart() {
        serverStart = System.nanoTime();
    }

    public void setServerEnd() {
        serverEnd = System.nanoTime();
    }
}
