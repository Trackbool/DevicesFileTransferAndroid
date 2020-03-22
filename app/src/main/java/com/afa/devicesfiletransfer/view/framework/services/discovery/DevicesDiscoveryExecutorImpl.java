package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSender;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSenderFactory;

import java.net.SocketException;

public class DevicesDiscoveryExecutorImpl implements DevicesDiscoveryExecutor {
    private final static int DISCOVERY_SERVICE_PORT = 5000;
    private final Context context;
    private DiscoveryProtocolSender discoverySender;

    public DevicesDiscoveryExecutorImpl(Context context) {
        this.context = context;
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(DISCOVERY_SERVICE_PORT);
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

    @Override
    public void discover() throws SocketException {
        discoverySender.discover();
    }
}
