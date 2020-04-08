package com.afa.devicesfiletransfer.services.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.TransferFile;

import java.util.List;

public interface FileSenderServiceExecutor {
    void send(List<Device> devices, TransferFile file);
}
