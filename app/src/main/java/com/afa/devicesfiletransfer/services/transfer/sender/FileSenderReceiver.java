package com.afa.devicesfiletransfer.services.transfer.sender;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;

import java.util.List;

public interface FileSenderReceiver {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(FileSenderProtocol.Callback callback);

    List<Transfer> getInProgressTransfers();

    void receive();

    void stop();
}
