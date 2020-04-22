package com.afa.devicesfiletransfer.domain.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Transfer implements Serializable {
    private Device device;
    private TransferFile file;
    private int progress;
    private Date date;
    private boolean incoming;
    private TransferStatus status;

    public Transfer() {
        date = new Date();
        status = TransferStatus.NOT_STARTED;
    }

    public Transfer(Device device, TransferFile file, int progress, boolean incoming) {
        this();
        this.device = device;
        this.file = file;
        this.progress = progress;
        this.incoming = incoming;
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

    public void setFile(TransferFile file) {
        this.file = file;
    }

    public TransferFile getFile() {
        return file;
    }

    //Method necessary to allow mapping with persistence entity
    public void setFilePath(String filePath) {
        file = TransferFileFactory.getFromFile(new File(filePath));
    }

    //Method necessary to allow mapping with persistence entity
    public String getFilePath() {
        return file.getPath();
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

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public enum TransferStatus {
        NOT_STARTED("Not started"),
        TRANSFERRING("Transferring"),
        FAILED("Failed"),
        COMPLETED("Completed"),
        CANCELED("Canceled");

        private String value;

        TransferStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(device, transfer.device) &&
                Objects.equals(file, transfer.file) &&
                Objects.equals(date, transfer.date) &&
                Objects.equals(incoming, transfer.incoming);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, file, date, incoming);
    }
}
