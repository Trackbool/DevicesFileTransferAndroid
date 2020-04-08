package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.view.framework.TransferFileImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesSenderService extends Service {
    public static final int START = 0;
    public static final int FAILURE = 1;
    public static final int PROGRESS_UPDATED = 2;
    public static final int SUCCESS = 3;
    private static final String CHANNEL_ID = FilesSenderService.class.getName() + "Channel";
    private ThreadPoolExecutor fileSendingExecutor;
    private final IBinder binder = new FilesSenderService.LocalBinder();
    private List<ResultReceiver> receivers = new ArrayList<>();

    public class LocalBinder extends Binder {
        FilesSenderService getService() {
            return FilesSenderService.this;
        }
    }

    public void addResultReceiver(ResultReceiver resultReceiver) {
        receivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        receivers.remove(resultReceiver);
    }

    public FilesSenderService() {
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Device device = intent.getParcelableExtra("device");
        TransferFileImpl file = intent.getParcelableExtra("file");
        file.setContext(getApplicationContext());

        final FileSenderProtocol fileSenderProtocol = createFileSender(device, file);
        fileSendingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fileSenderProtocol.send();
            }
        });

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sender")
                .setContentText("Sending files...")
                .setSmallIcon(R.drawable.icon_send)
                .build();

        startForeground(2, notification);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private FileSenderProtocol createFileSender(Device device, TransferFile file) {
        FileSenderProtocol fileSender = new FileSenderProtocol(device, file);
        final Bundle bundle = new Bundle();
        fileSender.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                //TODO: Notify the file is sending
                bundle.putSerializable("transfer", transfer);
                sendToAllReceivers(START, bundle);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                //TODO: Notify failure in transfer
                bundle.putSerializable("transfer", transfer);
                bundle.putSerializable("exception", e);
                sendToAllReceivers(FAILURE, bundle);
                finishServiceIfThereAreNoMoreTransfers();
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                //TODO: Update the progress in notification
                bundle.putSerializable("transfer", transfer);
                sendToAllReceivers(PROGRESS_UPDATED, bundle);
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                //TODO: Notify transfer succeeded
                bundle.putSerializable("transfer", transfer);
                bundle.putParcelable("file", (TransferFileImpl) file);
                sendToAllReceivers(SUCCESS, bundle);
                finishServiceIfThereAreNoMoreTransfers();
            }
        });

        return fileSender;
    }

    private void finishServiceIfThereAreNoMoreTransfers() {
        if (fileSendingExecutor.getActiveCount() == 1) {
            stopSelf();
        }
    }

    private void sendToAllReceivers(int resultCode, Bundle resultData) {
        for (ResultReceiver resultReceiver : receivers) {
            if (resultReceiver != null) {
                resultReceiver.send(resultCode, resultData);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        fileSendingExecutor.shutdownNow();
        super.onDestroy();
    }
}