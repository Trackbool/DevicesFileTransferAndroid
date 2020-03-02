package com.afa.devicesfiletransfer.view.framework.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

import java.io.File;

public class AndroidFileSenderServiceExecutorImpl implements FileSenderServiceExecutor {

    private final Context context;

    public AndroidFileSenderServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void send(Device device, File file) {
        startService(device, file);
    }

    private void startService(Device device, File file) {
        Intent serviceIntent = new Intent(context, FilesSenderService.class);
        serviceIntent.putExtra("device", device);
        serviceIntent.putExtra("file", file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
