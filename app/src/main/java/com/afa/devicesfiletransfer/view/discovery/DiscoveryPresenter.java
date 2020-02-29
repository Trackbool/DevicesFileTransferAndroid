package com.afa.devicesfiletransfer.view.discovery;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListenerFactory;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSender;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSenderFactory;

import java.net.InetAddress;
import java.net.SocketException;

public class DiscoveryPresenter implements DiscoveryContract.Presenter {
    private final static int DISCOVERY_SERVICE_PORT = 5000;
    private DiscoveryContract.View view;
    private DiscoveryProtocolListener discoveryListener;
    private DiscoveryProtocolSender discoverySender;

    public DiscoveryPresenter(DiscoveryContract.View view) {
        this.view = view;
    }

    @Override
    public void onViewLoaded() {
        discoveryListener = DiscoveryProtocolListenerFactory
                .getDefault(DISCOVERY_SERVICE_PORT, new DiscoveryProtocolListener.Callback() {
                    @Override
                    public void discoveryRequestReceived(InetAddress senderAddress, int senderPort) {
                        System.out.println("Received request from: " + senderAddress.getHostAddress());
                    }

                    @Override
                    public void discoveryResponseReceived(InetAddress senderAddress, int senderPort, DeviceProperties deviceProperties) {
                        String deviceName = deviceProperties.getName();
                        String os = deviceProperties.getOs();
                        Device device = new Device(deviceName, os, senderAddress);

                        if (!view.getDevicesList().contains(device)) {
                            view.addDevice(device);
                        }
                    }
                });
        try {
            discoveryListener.start();
        } catch (SocketException e) {
            view.showError("Initialization error", e.getMessage());
            view.close();
        }
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(DISCOVERY_SERVICE_PORT);
        discoverDevices();
    }

    @Override
    public void onDiscoverDevicesEvent() {
        discoverDevices();
    }

    private void discoverDevices() {
        try {
            discoverySender.discover();
            view.clearDevicesList();
        } catch (SocketException e) {
            view.showError("Discover error", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        discoveryListener.stop();
        view = null;
    }
}
