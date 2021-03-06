package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.afa.devicesfiletransfer.ConfigProperties;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceInteractor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSender;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSenderFactory;

import java.net.SocketException;

public class DiscoveryServiceInteractorImpl implements DiscoveryServiceInteractor {
    private final Context context;
    private boolean mBound = false;
    private ServiceConnectionCallback serviceConnectionCallback;
    private DiscoveryProtocolListener.Callback callback;
    private DevicesDiscoveryService boundService;

    private DiscoveryProtocolSender discoverySender;

    public DiscoveryServiceInteractorImpl(Context context) {
        this.context = context;
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(ConfigProperties.DISCOVERY_SERVICE_PORT);
    }

    @Override
    public void setServiceConnectionCallback(ServiceConnectionCallback callback) {
        this.serviceConnectionCallback = callback;
    }

    @Override
    public void setCallback(DiscoveryProtocolListener.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void receive() {
        Intent serviceIntent = new Intent(context, DevicesDiscoveryService.class);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void discover() throws SocketException {
        discoverySender.discover();
    }

    @Override
    public void stop() {
        if (boundService != null)
            boundService.removeCallbackReceiver(callback);
        if (mBound)
            context.unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DevicesDiscoveryService.LocalBinder binder = (DevicesDiscoveryService.LocalBinder) service;
            boundService = binder.getService();
            boundService.addCallbackReceiver(callback);
            mBound = true;

            if (serviceConnectionCallback != null) {
                serviceConnectionCallback.onConnect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;

            if (serviceConnectionCallback != null) {
                serviceConnectionCallback.onDisconnect();
            }
        }
    };
}
