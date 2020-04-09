package com.afa.devicesfiletransfer.domain.model;

import com.afa.devicesfiletransfer.util.SystemUtils;

import java.net.InetAddress;

public class DeviceFactory {
    public static Device getCurrentDevice(InetAddress deviceAddress) {
        String name = SystemUtils.getSystemName();
        String os = SystemUtils.getOs();
        return new Device(name, os, deviceAddress);
    }
}
