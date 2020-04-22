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
    @NotNull
    @Embedded(prefix = "device_")
    private DeviceEntity device;
    @NotNull
    private String filePath;
    @NotNull
    private int progress;
    @NotNull
    private Date date;
    @NotNull
    private String status;
    @NotNull
    private boolean incoming;

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

    @NotNull
    public DeviceEntity getDevice() {
        return device;
    }

    public void setDevice(@NotNull DeviceEntity device) {
        this.device = device;
    }

    @NotNull
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(@NotNull String filePath) {
        this.filePath = filePath;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @NotNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NotNull Date date) {
        this.date = date;
    }

    @NotNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NotNull String status) {
        this.status = status;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }
}
