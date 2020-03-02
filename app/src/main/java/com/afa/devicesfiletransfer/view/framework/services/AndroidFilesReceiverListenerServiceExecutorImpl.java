package com.afa.devicesfiletransfer.view.framework.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

public class AndroidFilesReceiverListenerServiceExecutorImpl implements FilesReceiverListenerServiceExecutor {

    private final Context context;

    public AndroidFilesReceiverListenerServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        startFilesReceiverListenerService();
    }

    private void startFilesReceiverListenerService() {
        Intent serviceIntent = new Intent(context, FilesReceiverListenerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    @Override
    public void stop() {
        Intent serviceIntent = new Intent(context, FilesReceiverListenerService.class);
        context.stopService(serviceIntent);
    }
}
