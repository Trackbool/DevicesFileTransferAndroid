package com.afa.devicesfiletransfer.services.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;

import java.io.File;

public interface FileSenderServiceExecutor {
    void send(Device device, File file);
}
