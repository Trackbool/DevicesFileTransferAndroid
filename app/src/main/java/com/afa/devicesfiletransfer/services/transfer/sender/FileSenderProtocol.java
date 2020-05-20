package com.afa.devicesfiletransfer.services.transfer.sender;

import com.afa.devicesfiletransfer.ConfigProperties;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.DeviceFactory;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class FileSenderProtocol {
    private final Device remoteDevice;
    private Device currentDevice;
    private final List<TransferFile> files;
    private Callback callback;
    private boolean isSending;

    public FileSenderProtocol(Device remoteDevice, List<TransferFile> files) {
        isSending = false;
        this.remoteDevice = remoteDevice;
        this.files = files;
    }

    public FileSenderProtocol(Device remoteDevice, List<TransferFile> files, Callback callback) {
        this(remoteDevice, files);
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Device getTargetDevice() {
        return remoteDevice;
    }

    public int getTransfersNum() {
        return files.size();
    }

    public boolean isSending() {
        return isSending;
    }

    public void send() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(remoteDevice.getAddress(), ConfigProperties.TRANSFER_SERVICE_PORT), 3000);
            InetAddress currentDeviceAddress = socket.getLocalAddress();
            currentDevice = DeviceFactory.getCurrentDevice(currentDeviceAddress);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            isSending = true;
            outputStream.writeInt(files.size());
            for (TransferFile file : files) {
                final Transfer transfer = new Transfer(
                        remoteDevice, file, 0, false);
                if (!file.exists()) {
                    transfer.setStatus(Transfer.TransferStatus.FAILED);
                    if (callback != null) {
                        callback.onTransferInitializationFailure(transfer,
                                new FileNotFoundException("File " + file.getPath() + " doesn´t " +
                                        "exists or cannot be accessed"));
                    }
                    continue;
                }
                final FileSender fileSender = createFileSender(transfer, callback);

                try {
                    sendFileData(file, outputStream);
                    fileSender.send(outputStream);
                } catch (IOException e) {
                    if (callback != null) {
                        callback.onFailure(transfer, e);
                    }
                }
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onInitializationFailure(this);
            }
        } finally {
            isSending = false;
        }
    }

    private void sendFileData(TransferFile file, DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(new Gson().toJson(currentDevice));
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());
    }

    private FileSender createFileSender(final Transfer transfer, final Callback callback) {
        FileSender.Callback fileSenderCallback = new FileSender.Callback() {
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
                callback.onSuccess(transfer, file);
            }
        };
        return new FileSender(transfer.getFile(), fileSenderCallback);
    }

    public interface Callback {
        void onInitializationFailure(FileSenderProtocol fileSenderProtocol);

        void onTransferInitializationFailure(Transfer transfer, Exception e);

        void onStart(Transfer transfer);

        void onFailure(Transfer transfer, Exception e);

        void onProgressUpdated(Transfer transfer);

        void onSuccess(Transfer transfer, TransferFile file);
    }
}
