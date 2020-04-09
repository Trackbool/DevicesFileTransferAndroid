package com.afa.devicesfiletransfer.framework.database.entities;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "transfer")
@TypeConverters({DateConverter.class})
public class TransferEntity {
    @PrimaryKey
    @NotNull
    private String id;
    @Embedded(prefix = "device_")
    private DeviceEntity device;
    private String fileName;
    private int progress;
    private Date date;
    private String status;

    public TransferEntity() {
        id = UUID.randomUUID().toString();
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public DeviceEntity getDevice() {
        return device;
    }

    public void setDevice(DeviceEntity device) {
        this.device = device;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
