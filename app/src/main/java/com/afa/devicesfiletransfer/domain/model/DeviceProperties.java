package com.afa.devicesfiletransfer.domain.model;

import java.io.Serializable;

public class DeviceProperties implements Serializable {
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
