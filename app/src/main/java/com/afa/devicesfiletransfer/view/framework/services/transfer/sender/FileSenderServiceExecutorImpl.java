package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.framework.TransferFileUri;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

import java.util.ArrayList;
import java.util.List;

public class FileSenderServiceExecutorImpl implements FileSenderServiceExecutor {
    private final Context context;

    public FileSenderServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void send(List<Device> devices, List<TransferFile> files) {
        Intent serviceIntent = new Intent(context, FilesSenderService.class);
        serviceIntent.putParcelableArrayListExtra("devices", new ArrayList<>(devices));
        serviceIntent.putParcelableArrayListExtra("files", getTransferFilesUriList(files));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private ArrayList<TransferFileUri> getTransferFilesUriList(List<TransferFile> files) {
        ArrayList<TransferFileUri> transfersFileUri = new ArrayList<>();
        for (TransferFile transferFile : files) {
            transfersFileUri.add((TransferFileUri) transferFile);
        }

        return transfersFileUri;
    }
}
