package com.afa.devicesfiletransfer.domain.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Transfer implements Serializable {
    private Device device;
    private String fileName;
    private int progress;
    private Date date;
    private TransferStatus status;

    public Transfer() {
        date = new Date();
        status = TransferStatus.NOT_STARTED;
    }

    public Transfer(Device device, String fileName, int progress) {
        this();
        this.device = device;
        this.fileName = fileName;
        this.progress = progress;
    }

    public void setDevice(Device device) {
        this.device = device;
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getProgressPercentage() {
        return progress + "%";
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(device, transfer.device) &&
                Objects.equals(fileName, transfer.fileName) &&
                Objects.equals(date, transfer.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, fileName, date);
    }
}
