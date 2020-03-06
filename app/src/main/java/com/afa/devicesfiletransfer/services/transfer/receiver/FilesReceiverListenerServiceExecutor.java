package com.afa.devicesfiletransfer.services.transfer.receiver;

public interface FilesReceiverListenerServiceExecutor {
    void setCallback(FileReceiverProtocol.Callback callback);

    void start();

    void stop();
}
