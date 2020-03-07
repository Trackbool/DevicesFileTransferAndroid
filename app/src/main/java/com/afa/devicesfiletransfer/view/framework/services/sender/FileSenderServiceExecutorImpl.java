package com.afa.devicesfiletransfer.view.framework.services.sender;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.model.TransferFileImpl;

public class FileSenderServiceExecutorImpl implements FileSenderServiceExecutor {
    private final Context context;
    private FileSenderProtocol.Callback callback;

    public FileSenderServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setCallback(FileSenderProtocol.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void send(Device device, TransferFile file) {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case FilesSenderService.START:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            callback.onStart(transfer);
                        }
                        break;
                    case FilesSenderService.PROGRESS_UPDATED:
                        if (callback != null) {
                            callback.onProgressUpdated();
                        }
                        break;
                    case FilesSenderService.FAILURE:
                        if (callback != null) {
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.onFailure(exception);
                        }
                        break;
                    case FilesSenderService.SUCCESS:
                        if (callback != null) {
                            TransferFile file = resultData.getParcelable("file");
                            callback.onSuccess(file);
                        }
                        break;
                }
                super.onReceiveResult(resultCode, resultData);
            }
        };

        Intent serviceIntent = new Intent(context, FilesSenderService.class);
        serviceIntent.putExtra("device", device);
        serviceIntent.putExtra("file", (TransferFileImpl) file);
        serviceIntent.putExtra("resultReceiver", resultReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
