package com.afa.devicesfiletransfer.view.discovery;

import com.afa.devicesfiletransfer.model.Device;

public interface DiscoveryContract {
    interface View {
        void addDevice(Device device);

        void showError(String title, String message);

        void clearDevicesList();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDiscoverDevicesEvent();

        void onDestroy();
    }
}
