package com.afa.devicesfiletransfer.framework.database.entities;

import org.jetbrains.annotations.NotNull;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "device")
public class DeviceEntity {
    @NotNull
    private String name;
    @NotNull
    private String os;

    public DeviceEntity() {

    }

    @Ignore
    public DeviceEntity(@NotNull String name, @NotNull String os) {
        this.name = name;
        this.os = os;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getOs() {
        return os;
    }

    public void setOs(@NotNull String os) {
        this.os = os;
    }
}
