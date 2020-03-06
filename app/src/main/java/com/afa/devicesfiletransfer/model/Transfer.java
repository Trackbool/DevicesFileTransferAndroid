package com.afa.devicesfiletransfer.model;

import java.io.Serializable;

public class Transfer implements Serializable {
    private Device device;
    private String fileName;
    private int progress;
    private TransferStatus status;

    public Transfer(Device device, String fileName, int progress) {
        this.device = device;
        this.fileName = fileName;
        this.progress = progress;
        status = TransferStatus.NOT_STARTED;
    }

    public Device getDevice() {
        return device;
    }

    public String getDeviceName() {
        return device.getName();
    }

    public String getDeviceIpAddress() {
        return device.getIpAddress();
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return fileName;
    }

    public int getProgress() {
        return progress;
    }

    public String getProgressPercentage() {
        return progress + "%";
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public String getStatusValue() {
        return status.value;
    }

    public enum TransferStatus {
        NOT_STARTED("Not started"),
        TRANSFERRING("Transferring"),
        FAILED("Failed"),
        SUCCEEDED("Succeeded"),
        CANCELED("Canceled");

        private String value;

        TransferStatus(String value) {
            this.value = value;
        }
    }
}
