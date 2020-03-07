package com.afa.devicesfiletransfer.view.presenters.discovery;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;

import java.net.InetAddress;
import java.net.SocketException;

public class DiscoveryPresenter implements DiscoveryContract.Presenter {
    private DiscoveryContract.View view;
    private DevicesDiscoveryExecutor devicesDiscoveryExecutor;

    public DiscoveryPresenter(final DiscoveryContract.View view, DevicesDiscoveryExecutor devicesDiscoveryExecutor) {
        this.view = view;
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
        devicesDiscoveryExecutor.setCallback(new DiscoveryProtocolListener.Callback() {
            @Override
            public void initializationFailure(Exception e) {
                view.showError("Initialization error", e.getMessage());
                view.close();
            }

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
    }

    @Override
    public void onViewLoaded() {
        devicesDiscoveryExecutor.start();
        discoverDevices();
    }

    @Override
    public void onDiscoverDevicesEvent() {
        discoverDevices();
    }

    private void discoverDevices() {
        try {
            devicesDiscoveryExecutor.discover();
            view.clearDevicesList();
        } catch (SocketException e) {
            view.showError("Discover error", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
