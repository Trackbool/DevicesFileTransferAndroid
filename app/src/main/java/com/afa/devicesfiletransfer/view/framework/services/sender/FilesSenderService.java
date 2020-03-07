package com.afa.devicesfiletransfer.view.framework.services.sender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.view.framework.model.TransferFileImpl;
import com.afa.devicesfiletransfer.view.framework.services.receiver.FilesReceiverListenerService;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesSenderService extends Service {
    public static final int START = 0;
    public static final int FAILURE = 1;
    public static final int PROGRESS_UPDATED = 2;
    public static final int SUCCESS = 3;
    private ResultReceiver receiver;
    private static final String CHANNEL_ID = FilesReceiverListenerService.class.getName() + "Channel";
    private ThreadPoolExecutor fileSendingExecutor;

    public FilesSenderService() {
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = intent.getParcelableExtra("resultReceiver");
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
                .setSmallIcon(R.drawable.send_icon)
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
                receiver.send(START, bundle);
            }

            @Override
            public void onFailure(Exception e) {
                //TODO: Notify failure in transfer
                bundle.putSerializable("exception", e);
                receiver.send(FAILURE, bundle);
                finishServiceIfThereAreNoMoreTransfers();
            }

            @Override
            public void onProgressUpdated() {
                //TODO: Update the progress in notification
                receiver.send(PROGRESS_UPDATED, bundle);
            }

            @Override
            public void onSuccess(TransferFile file) {
                //TODO: Notify transfer succeeded
                bundle.putParcelable("file", (TransferFileImpl) file);
                receiver.send(SUCCESS, bundle);
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