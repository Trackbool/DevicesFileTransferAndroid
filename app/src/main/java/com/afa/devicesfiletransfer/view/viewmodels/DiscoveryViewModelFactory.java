package com.afa.devicesfiletransfer.view.viewmodels;

import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DiscoveryViewModelFactory implements ViewModelProvider.Factory {
    private DevicesDiscoveryExecutor devicesDiscoveryExecutor;

    public DiscoveryViewModelFactory(DevicesDiscoveryExecutor devicesDiscoveryExecutor) {
        this.devicesDiscoveryExecutor = devicesDiscoveryExecutor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DiscoveryViewModel(devicesDiscoveryExecutor);
    }
}
