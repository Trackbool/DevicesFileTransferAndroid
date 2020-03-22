package com.afa.devicesfiletransfer.view.viewmodels.discovery;

import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryReceiver;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DiscoveryViewModelFactory implements ViewModelProvider.Factory {
    private DevicesDiscoveryExecutor devicesDiscoveryExecutor;
    private DevicesDiscoveryReceiver devicesDiscoveryReceiver;

    public DiscoveryViewModelFactory(DevicesDiscoveryExecutor devicesDiscoveryExecutor,
                                     DevicesDiscoveryReceiver devicesDiscoveryReceiver) {
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
        this.devicesDiscoveryReceiver = devicesDiscoveryReceiver;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DiscoveryViewModel(devicesDiscoveryExecutor, devicesDiscoveryReceiver);
    }
}
