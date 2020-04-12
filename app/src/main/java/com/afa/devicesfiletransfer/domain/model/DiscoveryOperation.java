package com.afa.devicesfiletransfer.domain.model;

public class DiscoveryOperation {
    private String name;
    private DeviceProperties deviceProperties;

    public DiscoveryOperation(String name, DeviceProperties deviceProperties) {
        this.name = name;
        this.deviceProperties = deviceProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceProperties getDeviceProperties() {
        return deviceProperties;
    }

    public void setDeviceProperties(DeviceProperties deviceProperties) {
        this.deviceProperties = deviceProperties;
    }
}
