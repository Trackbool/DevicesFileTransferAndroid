package com.afa.devicesfiletransfer.model;

import java.net.InetAddress;

public class Device {
    private String name;
    private String os;
    private InetAddress address;

    public Device(String name, String os, InetAddress address) {
        this.name = name;
        this.os = os;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getIpAddress() {
        return address.getHostAddress();
    }
}
