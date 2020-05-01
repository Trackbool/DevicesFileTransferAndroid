package com.afa.devicesfiletransfer.services.transfer.receiver;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFileFactory;
import com.afa.devicesfiletransfer.util.file.FileUtils;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiverProtocol {
    private File targetDirectory;
    private Callback callback;
    private boolean isReceiving;

    public FileReceiverProtocol(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        isReceiving = false;
    }

    public FileReceiverProtocol(File targetDirectory, Callback callback) {
        this(targetDirectory);
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public void receive(InputStream inputStream) {
        try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            isReceiving = true;
            int numberOfFiles = dataInputStream.readInt();

            for (int i = 0; i < numberOfFiles; i++) {
                try {
                    String deviceJson = dataInputStream.readUTF();
                    Device device = new Gson().fromJson(deviceJson, Device.class);
                    String fileNameWithExtension = dataInputStream.readUTF();
                    long fileSize = dataInputStream.readLong();
                    File file = createDestinationFile(fileNameWithExtension);
                    final Transfer transfer = new Transfer(
                            device, TransferFileFactory.getFromFile(file), 0, true);
                    FileReceiver fileReceiver = createFileReceiver(transfer, callback);
                    fileReceiver.receive(file, fileSize, inputStream);
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onInitializationFailure();
            }
        } finally {
            isReceiving = false;
        }
    }

    private File createDestinationFile(String fileNameWithExtension) {
        String destinationPath = targetDirectory.getAbsolutePath();
        String currentMillis = String.valueOf(System.currentTimeMillis());
        if (fileNameWithExtension == null || fileNameWithExtension.isEmpty()) {
            return new File(destinationPath, currentMillis);
        }

        String fileName = FileUtils.getFileNameWithoutExtension(fileNameWithExtension);
        String extension = FileUtils.getFileExtension(fileNameWithExtension);

        File destinationFile = new File(destinationPath, fileNameWithExtension);
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;
        while (destinationFile.exists() && attempts < MAX_ATTEMPTS) {
            String incrementedFileName = fileName + "(" + (attempts + 1) + ")." + extension;
            destinationFile = new File(destinationPath, incrementedFileName);
            attempts++;
        }

        if (attempts == MAX_ATTEMPTS && destinationFile.exists()) {
            destinationFile = new File(
                    destinationPath, fileName + "_" + currentMillis + "." + extension);
        }

        return destinationFile;
    }

    private FileReceiver createFileReceiver(final Transfer transfer, final Callback callback) {
        FileReceiver.Callback fileReceiverCallback = new FileReceiver.Callback() {
            @Override
            public void onStart() {
                transfer.setStatus(Transfer.TransferStatus.TRANSFERRING);
                callback.onStart(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                transfer.setStatus(Transfer.TransferStatus.FAILED);
                callback.onFailure(transfer, e);
            }

            @Override
            public void onProgressUpdated(int percentage) {
                transfer.setProgress(percentage);
                callback.onProgressUpdated(transfer);
            }

            @Override
            public void onSuccess(File file) {
                transfer.setStatus(Transfer.TransferStatus.COMPLETED);
                callback.onSuccess(transfer, file);
            }
        };
        return new FileReceiver(fileReceiverCallback);
    }

    public interface Callback {
        void onInitializationFailure();

        void onStart(Transfer transfer);

        void onFailure(Transfer transfer, Exception e);

        void onProgressUpdated(Transfer transfer);

        void onSuccess(Transfer transfer, File file);
    }
}
