package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceLauncher;

import java.util.ArrayList;
import java.util.List;

public class FileSenderServiceLauncherImpl implements FileSenderServiceLauncher {
    private final Context context;

    public FileSenderServiceLauncherImpl(Context context) {
        this.context = context;
    }

    @Override
    public void send(List<Device> devices, List<TransferFile> files) {
        Intent serviceIntent = new Intent(context, FilesSenderService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("devices", new ArrayList<>(devices));
        bundle.putSerializable("files", new ArrayList<>(files));
        serviceIntent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
