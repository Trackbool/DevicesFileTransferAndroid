package com.afa.devicesfiletransfer.services.transfer.receiver;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileReceiver {
    private final static int BUFFER_SIZE = 8192;
    private Callback callback;
    private final AtomicBoolean receiving;
    private Long fileSize;
    private AtomicLong receivedCount;

    public FileReceiver() {
        this.receiving = new AtomicBoolean(false);
        this.receivedCount = new AtomicLong(0);
    }

    public FileReceiver(Callback callback) {
        this();
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isReceiving() {
        return receiving.get();
    }

    public int getReceivedPercentage() {
        if (fileSize == null) throw new IllegalStateException("No transfer started");

        return (int) ((receivedCount.get() * 100) / fileSize);
    }

    public void receive(File targetFile, long fileSize, InputStream inputStream) {
        if (receiving.get()) throw new IllegalStateException("Already receiving the file");

        this.fileSize = fileSize;
        receiving.set(true);
        if (callback != null) {
            callback.onStart();
        }
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            receivedCount.set(0);
            int received;
            int currentPercentage = 0;
            while ((received = inputStream.read(buffer, 0, buffer.length)) != -1) {
                if (!receiving.get() || Thread.interrupted()) return;
                fileWriter.write(buffer, 0, received);
                receivedCount.getAndAdd(received);

                int receivedPercentage = getReceivedPercentage();
                if (callback != null && currentPercentage < receivedPercentage) {
                    currentPercentage = receivedPercentage;
                    callback.onProgressUpdated();
                }
            }
            if (callback != null) {
                if (receivedCount.get() == fileSize) {
                    callback.onSuccess(targetFile);
                } else {
                    callback.onFailure(new Exception("The file has not been completely transferred"));
                }
            }
        } catch (IOException e) {
            receiving.set(false);
            if (callback != null)
                callback.onFailure(e);
        } finally {
            receiving.set(false);
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void cancel() {
        receiving.set(false);
    }

    public interface Callback {
        void onStart();

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
