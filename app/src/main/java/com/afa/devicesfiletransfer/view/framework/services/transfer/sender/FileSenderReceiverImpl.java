package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;

import java.util.List;

public class FileSenderReceiverImpl implements FileSenderReceiver {
    private final Context context;
    private boolean mBound = false;
    private ServiceConnectionCallback serviceConnectionCallback;
    private FileSenderProtocol.Callback callback;
    private FilesSenderService boundService;
    private ResultReceiver resultReceiver;

    public FileSenderReceiverImpl(Context context) {
        this.context = context;
    }

    @Override
    public void setServiceConnectionCallback(ServiceConnectionCallback callback) {
        this.serviceConnectionCallback = callback;
    }

    @Override
    public void setCallback(FileSenderProtocol.Callback callback) {
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
        resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case FilesSenderService.INITIALIZATION_FAILURE:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.onInitializationFailure(transfer, exception);
                        }
                        break;
                    case FilesSenderService.START:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            callback.onStart(transfer);
                        }
                        break;
                    case FilesSenderService.PROGRESS_UPDATED:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            callback.onProgressUpdated(transfer);
                        }
                        break;
                    case FilesSenderService.FAILURE:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData
                                    .getSerializable("transfer");
                            Exception exception = (Exception) resultData.getSerializable("exception");
                            callback.onFailure(transfer, exception);
                        }
                        break;
                    case FilesSenderService.SUCCESS:
                        if (callback != null) {
                            Transfer transfer = (Transfer) resultData.getSerializable("transfer");
                            TransferFile file = resultData.getParcelable("file");
                            callback.onSuccess(transfer, file);
                        }
                        break;
                }
                super.onReceiveResult(resultCode, resultData);
            }
        };

        Intent serviceIntent = new Intent(context, FilesSenderService.class);
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
            FilesSenderService.LocalBinder binder = (FilesSenderService.LocalBinder) service;
            boundService = binder.getService();
            boundService.addResultReceiver(resultReceiver);
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
