package com.afa.devicesfiletransfer.domain.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class Device implements Serializable {
    private String name;
    private String os;
    private InetAddress address;

    public Device() {
        address = InetAddress.getLoopbackAddress();
    }

    public Device(String name, String os, InetAddress address) {
        this.name = name;
        this.os = os;
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs() {
        return os;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getIpAddress() {
        return address.getHostAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return address.equals(device.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
