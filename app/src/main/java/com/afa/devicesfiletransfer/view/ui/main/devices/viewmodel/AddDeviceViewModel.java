package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.services.discovery.NetworkDataProvider;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.model.ErrorModel;

import java.net.InetAddress;
import java.net.UnknownHostException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddDeviceViewModel extends ViewModel {
    private final MutableLiveData<Device> deviceLiveData;
    private final LiveEvent<ErrorModel> onErrorEvent;

    public AddDeviceViewModel() {
        deviceLiveData = new MutableLiveData<>();
        onErrorEvent = new LiveEvent<>();
    }

    public LiveData<Device> getDeviceLiveData() {
        return deviceLiveData;
    }

    public LiveEvent<ErrorModel> getOnErrorEvent() {
        return onErrorEvent;
    }

    public void onAddDeviceButtonClicked(final String name, final String ipAddress) {
        if (name == null || name.isEmpty()) {
            onErrorEvent.postValue(
                    new ErrorModel("Error", "The name can´t be empty"));
            return;
        }

        if (ipAddress == null || ipAddress.isEmpty()) {
            onErrorEvent.postValue(
                    new ErrorModel("Error", "The IP Address can´t be empty"));
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress deviceIpAddress = InetAddress.getByName(ipAddress);
                    NetworkDataProvider networkDataProvider = new NetworkDataProvider();
                    if (!networkDataProvider.isCurrentDeviceIp(deviceIpAddress)) {
                        deviceLiveData.postValue(new Device(name, "Unknown", deviceIpAddress));
                    } else {
                        onErrorEvent.postValue(new ErrorModel("Error",
                                "The IP Address can´t be the current device address"));
                    }
                } catch (UnknownHostException e) {
                    onErrorEvent.postValue(
                            new ErrorModel("Error", "The IP Address is invalid"));
                }
            }
        }).start();
    }
}
