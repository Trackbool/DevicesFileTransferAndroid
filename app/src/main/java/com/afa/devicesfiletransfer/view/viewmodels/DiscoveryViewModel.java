package com.afa.devicesfiletransfer.view.viewmodels;

import android.util.Pair;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DiscoveryViewModel extends ViewModel {
    private final List<Device> devices;
    private final MutableLiveData<List<Device>> devicesLiveData;
    private final LiveEvent<InetAddress> discoveryRequestReceivedEvent;
    private final LiveEvent<Pair<String, String>> errorEvent;
    private final DevicesDiscoveryExecutor devicesDiscoveryExecutor;

    public DiscoveryViewModel(DevicesDiscoveryExecutor devicesDiscoveryExecutor) {
        devices = new ArrayList<>();
        devicesLiveData = new MutableLiveData<>();
        discoveryRequestReceivedEvent = new LiveEvent<>();
        errorEvent = new LiveEvent<>();
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
        devicesDiscoveryExecutor.setCallback(new DiscoveryProtocolListener.Callback() {
            @Override
            public void initializationFailure(Exception e) {
                showError("Initialization error", e.getMessage());
            }

            @Override
            public void discoveryRequestReceived(InetAddress senderAddress, int senderPort) {
                discoveryRequestReceivedEvent.postValue(senderAddress);
            }

            @Override
            public void discoveryResponseReceived(InetAddress senderAddress, int senderPort, DeviceProperties deviceProperties) {
                String deviceName = deviceProperties.getName();
                String os = deviceProperties.getOs();
                Device device = new Device(deviceName, os, senderAddress);

                devices.add(device);
                devicesLiveData.postValue(devices);
            }
        });
        devicesDiscoveryExecutor.start();
        discoverDevices();
    }

    public MutableLiveData<List<Device>> getDevicesLiveData() {
        return devicesLiveData;
    }

    public LiveEvent<InetAddress> getDiscoveryRequestReceivedEvent() {
        return discoveryRequestReceivedEvent;
    }

    public LiveEvent<Pair<String, String>> getErrorEvent() {
        return errorEvent;
    }

    public void discoverDevices() {
        try {
            devicesDiscoveryExecutor.discover();
            devices.clear();
            devicesLiveData.postValue(devices);
        } catch (SocketException e) {
            showError("Discover error", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        errorEvent.postValue(new Pair<>(title, message));
    }
}
