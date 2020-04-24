package com.afa.devicesfiletransfer.services.transfer.receiver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

            while ((received = inputStream.read(buffer, 0, getRemaining())) != -1
                    && receivedCount.get() < fileSize) {
                if (!receiving.get() || Thread.interrupted()) return;
                fileWriter.write(buffer, 0, received);
                receivedCount.getAndAdd(received);

                int receivedPercentage = getReceivedPercentage();
                if (callback != null && currentPercentage < receivedPercentage) {
                    currentPercentage = receivedPercentage;
                    callback.onProgressUpdated(currentPercentage);
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
        }
    }

    private int getRemaining() {
        long remaining = (fileSize - receivedCount.get());
        if (remaining > BUFFER_SIZE) {
            return BUFFER_SIZE;
        }

        return Math.max((int) remaining, 0);
    }

    public void cancel() {
        receiving.set(false);
    }

    public interface Callback {
        void onStart();

        void onFailure(Exception e);

        void onProgressUpdated(int percentage);

        void onSuccess(File file);
    }
}
