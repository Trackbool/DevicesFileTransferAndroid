package com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel;

import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceLauncher;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceInteractor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DevicesViewModelFactory implements ViewModelProvider.Factory {
    private DiscoveryServiceLauncher discoveryServiceLauncher;
    private DiscoveryServiceInteractor discoveryServiceInteractor;

    public DevicesViewModelFactory(DiscoveryServiceLauncher discoveryServiceLauncher,
                                   DiscoveryServiceInteractor discoveryServiceInteractor) {
        this.discoveryServiceLauncher = discoveryServiceLauncher;
        this.discoveryServiceInteractor = discoveryServiceInteractor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DevicesViewModel(discoveryServiceLauncher, discoveryServiceInteractor);
    }
}
