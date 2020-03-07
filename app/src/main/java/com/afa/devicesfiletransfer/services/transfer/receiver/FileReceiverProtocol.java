package com.afa.devicesfiletransfer.services.transfer.receiver;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiverProtocol {
    private File targetDirectory;
    private Callback callback;
    private FileReceiver fileReceiver;
    private Transfer transfer;

    public FileReceiverProtocol(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        fileReceiver = new FileReceiver();
    }

    public FileReceiverProtocol(File targetDirectory, Callback callback) {
        this.targetDirectory = targetDirectory;
        this.callback = callback;
        this.fileReceiver = createFileReceiver(callback);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
        this.fileReceiver = createFileReceiver(callback);
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public boolean isReceiving() {
        return fileReceiver.isReceiving();
    }

    public int getSentPercentage() {
        return fileReceiver.getReceivedPercentage();
    }

    public void receive(InputStream inputStream) {
        try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            String deviceJson = dataInputStream.readUTF();
            Device device = new Gson().fromJson(deviceJson, Device.class);
            String fileNameWithExtension = dataInputStream.readUTF();
            String fileName = generateFileName(fileNameWithExtension);
            long fileSize = dataInputStream.readLong();
            transfer = new Transfer(device, fileName, 0);

            File file = new File(targetDirectory.getAbsolutePath(), fileName);
            fileReceiver.receive(file, fileSize, inputStream);
        } catch (IOException e) {
            if (callback != null)
                callback.onFailure(e);
        }
    }

    private String generateFileName(String fileNameWithExtension) {
        if (fileNameWithExtension == null || fileNameWithExtension.isEmpty()) {
            return String.valueOf(System.currentTimeMillis());
        }

        String[] tokens = fileNameWithExtension.split("\\.(?=[^\\.]+$)");
        String resultFileName = tokens[0] + "_" + System.currentTimeMillis();
        if (tokens.length > 1) {
            String extension = tokens[1];
            resultFileName = resultFileName + "." + extension;
        }
        return resultFileName;
    }

    public void cancel() {
        fileReceiver.cancel();
    }

    private FileReceiver createFileReceiver(final Callback callback) {
        FileReceiver.Callback fileReceiverCallback = new FileReceiver.Callback() {
            @Override
            public void onStart() {
                transfer.setStatus(Transfer.TransferStatus.TRANSFERRING);
                callback.onStart(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                transfer.setStatus(Transfer.TransferStatus.FAILED);
                callback.onFailure(e);
            }

            @Override
            public void onProgressUpdated() {
                transfer.setProgress(fileReceiver.getReceivedPercentage());
                callback.onProgressUpdated();
            }

            @Override
            public void onSuccess(File file) {
                transfer.setStatus(Transfer.TransferStatus.SUCCEEDED);
                callback.onSuccess(file);
            }
        };
        return new FileReceiver(fileReceiverCallback);
    }

    public interface Callback {
        void onStart(Transfer transfer);

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
