package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverInteractor;

import java.util.List;

public class FilesReceiverInteractorImpl implements FilesReceiverInteractor {

    private final Context context;
    private boolean mBound = false;
    private ServiceConnectionCallback serviceConnectionCallback;
    private FileReceiverProtocol.Callback callback;
    private FilesReceiverListenerService boundService;

    public FilesReceiverInteractorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setServiceConnectionCallback(ServiceConnectionCallback callback) {
        this.serviceConnectionCallback = callback;
    }

    @Override
    public void setCallback(FileReceiverProtocol.Callback callback) {
        this.callback = callback;
    }

    @Override
    public List<Transfer> getInProgressTransfers() {
        if (!mBound) {
            throw new IllegalStateException("The service has not been started");
        }

        return boundService.getInProgressTransfers();
    }

    @Override
    public void receive() {
        Intent serviceIntent = new Intent(context, FilesReceiverListenerService.class);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
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
            FilesReceiverListenerService.LocalBinder binder = (FilesReceiverListenerService.LocalBinder) service;
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
