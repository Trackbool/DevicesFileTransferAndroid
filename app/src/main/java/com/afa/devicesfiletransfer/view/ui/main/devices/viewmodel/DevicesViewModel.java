package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceInteractor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceLauncher;
import com.afa.devicesfiletransfer.services.discovery.NetworkDataProvider;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.model.ErrorModel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DevicesViewModel extends ViewModel {
    private final MutableLiveData<String> currentDeviceAddress;
    private final List<Device> devices;
    private final MutableLiveData<List<Device>> devicesLiveData;
    private final LiveEvent<Device> discoveryRequestReceivedEvent;
    private final LiveEvent<ErrorModel> errorEvent;
    private DiscoveryServiceLauncher discoveryServiceLauncher;
    private DiscoveryServiceInteractor discoveryServiceInteractor;
    private final NetworkDataProvider networkDataProvider;

    private static final String NOT_CONNECTED = "Not connected";
    private static final int REFRESH_DEVICE_ADDRESS_RATE = 3000;
    private Timer refreshCurrentDeviceAddressTimer;

    public DevicesViewModel(DiscoveryServiceLauncher discoveryServiceLauncher,
                            DiscoveryServiceInteractor discoveryServiceInteractor) {
        currentDeviceAddress = new MutableLiveData<>();
        currentDeviceAddress.postValue(NOT_CONNECTED);
        devices = new ArrayList<>();
        devicesLiveData = new MutableLiveData<>();
        discoveryRequestReceivedEvent = new LiveEvent<>();
        errorEvent = new LiveEvent<>();
        this.discoveryServiceInteractor = discoveryServiceInteractor;
        this.discoveryServiceLauncher = discoveryServiceLauncher;
        networkDataProvider = new NetworkDataProvider();
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
                addOrUpdateDeviceIfAlreadyPresent(device);
            }

            @Override
            public void discoveryResponseReceived(Device device) {
                addOrUpdateDeviceIfAlreadyPresent(device);
            }

            @Override
            public void discoveryDisconnect(Device device) {
                devices.remove(device);
                devicesLiveData.postValue(devices);
            }
        };
        this.discoveryServiceInteractor.setCallback(discoveryProtocolCallback);
    }

    public MutableLiveData<String> getCurrentDeviceAddress() {
        return currentDeviceAddress;
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

    public void onStart() {
        this.discoveryServiceInteractor.receive();
    }

    public void onShowView() {
        refreshCurrentDeviceAddressTimer = new Timer();
        refreshCurrentDeviceAddressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateCurrentDeviceAddress();
            }
        }, 0, REFRESH_DEVICE_ADDRESS_RATE);
    }

    public void onHideView() {
        refreshCurrentDeviceAddressTimer.cancel();
    }

    public void addDevice(Device device) {
        addOrUpdateDeviceIfAlreadyPresent(device);
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

    private void addOrUpdateDeviceIfAlreadyPresent(Device device) {
        devices.remove(device);
        devices.add(device);
        devicesLiveData.postValue(devices);
    }

    private void updateCurrentDeviceAddress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String addressToShow = NOT_CONNECTED;
                try {
                    InetAddress currentAddress = networkDataProvider.getOutgoingDeviceIp();
                    if (networkDataProvider.isCurrentDeviceAddress(currentAddress)) {
                        addressToShow = currentAddress.getHostAddress();
                    }
                } catch (IOException ignored) {
                } finally {
                    if (!addressToShow.equals(currentDeviceAddress.getValue())) {
                        currentDeviceAddress.postValue(addressToShow);
                    }
                }
            }
        }).start();
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
