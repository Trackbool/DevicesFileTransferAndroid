package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryReceiver;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;

import java.net.InetAddress;

public class DevicesDiscoveryReceiverImpl implements DevicesDiscoveryReceiver {

    private final Context context;
    private boolean mBound = false;
    private DiscoveryProtocolListener.Callback callback;
    private DevicesDiscoveryService boundService;
    private ResultReceiver resultReceiver;

    public DevicesDiscoveryReceiverImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setCallback(DiscoveryProtocolListener.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void receive() {
        resultReceiver = new ResultReceiver(new Handler()) {
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
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stop() {
        if (boundService != null)
            boundService.removeResultReceiver(resultReceiver);
        resultReceiver = null;
        if (mBound)
            context.unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DevicesDiscoveryService.LocalBinder binder = (DevicesDiscoveryService.LocalBinder) service;
            boundService = binder.getService();
            boundService.addResultReceiver(resultReceiver);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}