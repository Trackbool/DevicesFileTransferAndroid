package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

public class FilesReceiverListenerServiceExecutorImpl implements FilesReceiverListenerServiceExecutor {
    private final Context context;

    public FilesReceiverListenerServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
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
