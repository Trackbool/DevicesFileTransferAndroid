package com.afa.devicesfiletransfer.services.transfer.sender;

public interface FileSenderReceiver {
    void setCallback(FileSenderProtocol.Callback callback);

    void receive();

    void stop();
}
