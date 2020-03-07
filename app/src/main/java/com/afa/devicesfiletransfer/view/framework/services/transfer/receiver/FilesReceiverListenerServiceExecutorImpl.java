package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

import java.io.File;

public class FilesReceiverListenerServiceExecutorImpl implements FilesReceiverListenerServiceExecutor {
    private final Context context;
    private FileReceiverProtocol.Callback callback;

    public FilesReceiverListenerServiceExecutorImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setCallback(FileReceiverProtocol.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void start() {
        startFilesReceiverListenerService();
    }

    private void startFilesReceiverListenerService() {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case FilesReceiverListenerService.START:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            callback.onStart(transfer);
                        }
                        break;
                    case FilesReceiverListenerService.PROGRESS_UPDATED:
                        if (callback != null) {
                            callback.onProgressUpdated();
                        }
                        break;
                    case FilesReceiverListenerService.FAILURE:
                        if (callback != null) {
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.onFailure(exception);
                        }
                        break;
                    case FilesReceiverListenerService.SUCCESS:
                        if (callback != null) {
                            File file = (File) resultData.getSerializable("file");
                            callback.onSuccess(file);
                        }
                        break;
                }
                super.onReceiveResult(resultCode, resultData);
            }
        };

        Intent serviceIntent = new Intent(context, FilesReceiverListenerService.class);
        serviceIntent.putExtra("resultReceiver", resultReceiver);
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
