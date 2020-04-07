package com.afa.devicesfiletransfer.services.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.TransferFile;

public interface FileSenderServiceExecutor {
    void send(Device device, TransferFile file);
}
