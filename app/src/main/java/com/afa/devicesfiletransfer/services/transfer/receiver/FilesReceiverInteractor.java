package com.afa.devicesfiletransfer.services.transfer.receiver;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;

import java.util.List;

public interface FilesReceiverInteractor {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(FileReceiverProtocol.Callback callback);

    List<Transfer> getInProgressTransfers();

    void receive();

    void stop();
}
