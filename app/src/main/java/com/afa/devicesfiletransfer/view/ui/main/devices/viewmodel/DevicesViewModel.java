package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceInteractor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceLauncher;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.model.ErrorModel;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {
    private final List<Device> devices;
    private final MutableLiveData<List<Device>> devicesLiveData;
    private final LiveEvent<Device> discoveryRequestReceivedEvent;
    private final LiveEvent<ErrorModel> errorEvent;
    private DiscoveryServiceLauncher discoveryServiceLauncher;
    private DiscoveryServiceInteractor discoveryServiceInteractor;

    public DevicesViewModel(DiscoveryServiceLauncher discoveryServiceLauncher,
                            DiscoveryServiceInteractor discoveryServiceInteractor) {
        devices = new ArrayList<>();
        devicesLiveData = new MutableLiveData<>();
        discoveryRequestReceivedEvent = new LiveEvent<>();
        errorEvent = new LiveEvent<>();
        this.discoveryServiceInteractor = discoveryServiceInteractor;
        this.discoveryServiceLauncher = discoveryServiceLauncher;
        this.discoveryServiceLauncher.start();
        this.discoveryServiceInteractor.setServiceConnectionCallback(new ServiceConnectionCallback() {
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
            public void discoveryRequestReceived(Device device) {
                addDeviceIfNotAlreadyInTheList(device);
            }

            @Override
            public void discoveryResponseReceived(Device device) {
                addDeviceIfNotAlreadyInTheList(device);
            }

            @Override
            public void discoveryDisconnect(Device device) {
                devices.remove(device);
                devicesLiveData.postValue(devices);
            }
        };
        this.discoveryServiceInteractor.setCallback(discoveryProtocolCallback);
    }

    public void onStart() {
        this.discoveryServiceInteractor.receive();
    }

    public MutableLiveData<List<Device>> getDevicesLiveData() {
        return devicesLiveData;
    }

    public LiveEvent<Device> getDiscoveryRequestReceivedEvent() {
        return discoveryRequestReceivedEvent;
    }

    public LiveEvent<ErrorModel> getErrorEvent() {
        return errorEvent;
    }

    public void addDevice(Device device) {
        addDeviceIfNotAlreadyInTheList(device);
    }

    public void discoverDevices() {
        try {
            discoveryServiceInteractor.discover();
            devices.clear();
            devicesLiveData.postValue(devices);
        } catch (SocketException e) {
            triggerErrorEvent("Discover error", e.getMessage());
        }
    }

    private void addDeviceIfNotAlreadyInTheList(Device device) {
        if (!devices.contains(device)) {
            devices.add(device);
            devicesLiveData.postValue(devices);
        }
    }

    private void triggerErrorEvent(String title, String message) {
        errorEvent.postValue(new ErrorModel(title, message));
    }

    public void onDestroy() {
        if (discoveryServiceInteractor != null)
            this.discoveryServiceInteractor.stop();
    }

    @Override
    protected void onCleared() {
        discoveryServiceInteractor.setServiceConnectionCallback(null);
        discoveryServiceInteractor.setCallback(null);
        super.onCleared();
    }
}
