package com.afa.devicesfiletransfer.services.transfer.receiver;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.domain.model.TransferFileFactory;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiverProtocol {
    private Callback callback;
    private boolean isReceiving;

    public FileReceiverProtocol() {
        isReceiving = false;
    }

    public FileReceiverProtocol(Callback callback) {
        this();
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
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
                    TransferFile file = createDestinationFile(fileNameWithExtension);
                    final Transfer transfer = new Transfer(
                            device, file, 0, true);
                    FileReceiver fileReceiver = createFileReceiver(transfer, callback);
                    fileReceiver.receive(file, fileSize, inputStream, file.getOutputStream());
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

    private TransferFile createDestinationFile(String fileNameWithExtension) {
        String currentMillis = String.valueOf(System.currentTimeMillis());
        if (fileNameWithExtension == null || fileNameWithExtension.isEmpty()) {
            return TransferFileFactory.createIncomingTransferFile(currentMillis);
        }

        return TransferFileFactory.createIncomingTransferFile(fileNameWithExtension);
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
            public void onSuccess(TransferFile file) {
                transfer.setStatus(Transfer.TransferStatus.COMPLETED);
                callback.onSuccess(transfer);
            }
        };
        return new FileReceiver(fileReceiverCallback);
    }

    public interface Callback {
        void onInitializationFailure();

        void onStart(Transfer transfer);

        void onFailure(Transfer transfer, Exception e);

        void onProgressUpdated(Transfer transfer);

        void onSuccess(Transfer transfer);
    }
}
