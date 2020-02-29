package com.afa.devicesfiletransfer.model;

public class DeviceProperties {
    private String name;
    private String os;

    public DeviceProperties(String name, String os) {
        this.name = name;
        this.os = os;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }
}
