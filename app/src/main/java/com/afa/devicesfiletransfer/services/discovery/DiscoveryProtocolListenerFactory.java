package com.afa.devicesfiletransfer.services.discovery;

import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.util.SystemUtils;

public class DiscoveryProtocolListenerFactory {
    public static DiscoveryProtocolListener getDefault(int port) {
        DeviceProperties deviceProperties = getDeviceProperties();
        return new DiscoveryProtocolListener(new NetworkDataProvider(), deviceProperties, port);
    }

    public static DiscoveryProtocolListener getDefault(int port, DiscoveryProtocolListener.Callback callback) {
        DeviceProperties deviceProperties = getDeviceProperties();
        return new DiscoveryProtocolListener(new NetworkDataProvider(), deviceProperties, port, callback);
    }

    private static DeviceProperties getDeviceProperties() {
        String systemName = SystemUtils.getSystemName();
        String systemOs = SystemUtils.getOs();
        return new DeviceProperties(systemName, systemOs);
    }
}
