package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.TransferFileImpl;

import java.util.ArrayList;
import java.util.List;

public class FileSenderServiceExecutorImpl implements FileSenderServiceExecutor {
    private final Context context;

    public FileSenderServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void send(List<Device> devices, TransferFile file) {
        Intent serviceIntent = new Intent(context, FilesSenderService.class);
        serviceIntent.putParcelableArrayListExtra("devices", new ArrayList<>(devices));
        serviceIntent.putExtra("file", (TransferFileImpl) file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
