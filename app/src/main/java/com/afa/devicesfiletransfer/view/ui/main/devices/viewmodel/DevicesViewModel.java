package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryReceiver;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.model.ErrorModel;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {
    private final List<Device> devices;
    private final MutableLiveData<List<Device>> devicesLiveData;
    private final LiveEvent<InetAddress> discoveryRequestReceivedEvent;
    private final LiveEvent<ErrorModel> errorEvent;
    private DevicesDiscoveryExecutor devicesDiscoveryExecutor;
    private DevicesDiscoveryReceiver devicesDiscoveryReceiver;

    public DevicesViewModel(DevicesDiscoveryExecutor devicesDiscoveryExecutor,
                            DevicesDiscoveryReceiver devicesDiscoveryReceiver) {
        devices = new ArrayList<>();
        devicesLiveData = new MutableLiveData<>();
        discoveryRequestReceivedEvent = new LiveEvent<>();
        errorEvent = new LiveEvent<>();
        this.devicesDiscoveryReceiver = devicesDiscoveryReceiver;
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
        this.devicesDiscoveryExecutor.start();
        this.devicesDiscoveryReceiver.setServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onConnect() {
                discoverDevices();
            }

            @Override
            public void onDisconnect() {

            }
        });
        DiscoveryProtocolListener.Callback discoveryProtocolCallback = new DiscoveryProtocolListener.Callback() {
            @Override
            public void initializationFailure(Exception e) {
                triggerErrorEvent("Initialization error", e.getMessage());
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
        };
        this.devicesDiscoveryReceiver.setCallback(discoveryProtocolCallback);
    }

    public void onStart() {
        this.devicesDiscoveryReceiver.receive();
    }

    public MutableLiveData<List<Device>> getDevicesLiveData() {
        return devicesLiveData;
    }

    public LiveEvent<InetAddress> getDiscoveryRequestReceivedEvent() {
        return discoveryRequestReceivedEvent;
    }

    public LiveEvent<ErrorModel> getErrorEvent() {
        return errorEvent;
    }

    public void discoverDevices() {
        try {
            devicesDiscoveryExecutor.discover();
            devices.clear();
            devicesLiveData.postValue(devices);
        } catch (SocketException e) {
            triggerErrorEvent("Discover error", e.getMessage());
        }
    }

    private void triggerErrorEvent(String title, String message) {
        errorEvent.postValue(new ErrorModel(title, message));
    }

    public void onDestroy() {
        if (devicesDiscoveryReceiver != null)
            this.devicesDiscoveryReceiver.stop();
    }

    @Override
    protected void onCleared() {
        devicesDiscoveryReceiver.setServiceConnectionCallback(null);
        devicesDiscoveryReceiver.setCallback(null);
        super.onCleared();
    }
}
