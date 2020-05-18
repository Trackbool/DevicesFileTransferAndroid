package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryInteractor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DevicesViewModelFactory implements ViewModelProvider.Factory {
    private DevicesDiscoveryExecutor devicesDiscoveryExecutor;
    private DevicesDiscoveryInteractor devicesDiscoveryInteractor;

    public DevicesViewModelFactory(DevicesDiscoveryExecutor devicesDiscoveryExecutor,
                                   DevicesDiscoveryInteractor devicesDiscoveryInteractor) {
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
        this.devicesDiscoveryInteractor = devicesDiscoveryInteractor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DevicesViewModel(devicesDiscoveryExecutor, devicesDiscoveryInteractor);
    }
}
