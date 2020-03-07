package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSender;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSenderFactory;

import java.net.InetAddress;
import java.net.SocketException;

public class DevicesDiscoveryExecutorImpl implements DevicesDiscoveryExecutor {
    private final static int DISCOVERY_SERVICE_PORT = 5000;
    private final Context context;
    private DiscoveryProtocolListener.Callback callback;
    private DiscoveryProtocolSender discoverySender;

    public DevicesDiscoveryExecutorImpl(Context context) {
        this.context = context;
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(DISCOVERY_SERVICE_PORT);
    }

    @Override
    public void setCallback(DiscoveryProtocolListener.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void start() {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case DevicesDiscoveryService.INITIALIZATION_FAILURE:
                        if (callback != null) {
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.initializationFailure(exception);
                        }
                        break;
                    case DevicesDiscoveryService.REQUEST_RECEIVED:
                        if (callback != null) {
                            InetAddress senderAddress = (InetAddress) resultData.getSerializable("senderAddress");
                            int senderPort = resultData.getInt("senderPort");
                            callback.discoveryRequestReceived(senderAddress, senderPort);
                        }
                        break;
                    case DevicesDiscoveryService.RESPONSE_RECEIVED:
                        if (callback != null) {
                            InetAddress senderAddress = (InetAddress) resultData.getSerializable("senderAddress");
                            int senderPort = resultData.getInt("senderPort");
                            DeviceProperties deviceProperties = (DeviceProperties)
                                    resultData.getSerializable("deviceProperties");
                            callback.discoveryResponseReceived(senderAddress, senderPort, deviceProperties);
                        }
                        break;
                }
                super.onReceiveResult(resultCode, resultData);
            }
        };

        Intent serviceIntent = new Intent(context, DevicesDiscoveryService.class);
        serviceIntent.putExtra("port", DISCOVERY_SERVICE_PORT);
        serviceIntent.putExtra("resultReceiver", resultReceiver);
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
