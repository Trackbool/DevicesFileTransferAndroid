package com.afa.devicesfiletransfer.services.transfer.receiver;

public interface FilesReceiverListenerReceiver {
    void setCallback(FileReceiverProtocol.Callback callback);

    void receive();

    void stop();
}
