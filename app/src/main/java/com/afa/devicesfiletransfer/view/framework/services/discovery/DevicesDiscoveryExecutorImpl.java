package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;

public class DevicesDiscoveryExecutorImpl implements DevicesDiscoveryExecutor {
    private final Context context;

    public DevicesDiscoveryExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        Intent serviceIntent = new Intent(context, DevicesDiscoveryService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    @Override
    public void stop() {
        Intent serviceIntent = new Intent(context, DevicesDiscoveryService.class);
        context.stopService(serviceIntent);
    }
}
