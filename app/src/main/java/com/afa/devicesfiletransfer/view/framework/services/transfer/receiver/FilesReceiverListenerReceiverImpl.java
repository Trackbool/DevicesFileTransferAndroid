package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;

import java.io.File;

public class FilesReceiverListenerReceiverImpl implements FilesReceiverListenerReceiver {

    private final Context context;
    private boolean mBound = false;
    private FileReceiverProtocol.Callback callback;
    private FilesReceiverListenerService boundService;
    private ResultReceiver resultReceiver;

    public FilesReceiverListenerReceiverImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setCallback(FileReceiverProtocol.Callback callback) {
        this.callback = callback;
    }

    @Override
    public void receive() {
        resultReceiver = new ResultReceiver(new Handler()) {
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
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            callback.onProgressUpdated(transfer);
                        }
                        break;
                    case FilesReceiverListenerService.FAILURE:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.onFailure(transfer, exception);
                        }
                        break;
                    case FilesReceiverListenerService.SUCCESS:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            File file = (File) resultData.getSerializable("file");
                            callback.onSuccess(transfer, file);
                        }
                        break;
                }
                super.onReceiveResult(resultCode, resultData);
            }
        };

        Intent serviceIntent = new Intent(context, FilesReceiverListenerService.class);
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
            FilesReceiverListenerService.LocalBinder binder = (FilesReceiverListenerService.LocalBinder) service;
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
