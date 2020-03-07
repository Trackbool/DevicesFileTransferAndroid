package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;
import com.afa.devicesfiletransfer.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesReceiverListenerService extends Service {
    public static final int START = 0;
    public static final int FAILURE = 1;
    public static final int PROGRESS_UPDATED = 2;
    public static final int SUCCESS = 3;
    private ResultReceiver receiver;
    private static final String CHANNEL_ID = FilesReceiverListenerService.class.getName() + "Channel";
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileReceivingExecutor;

    public FilesReceiverListenerService() {
        fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        filesReceiverListener = new FilesReceiverListener(TRANSFER_SERVICE_PORT, new FilesReceiverListener.Callback() {
            @Override
            public void onTransferReceived(final InputStream inputStream) {
                final FileReceiverProtocol fileReceiver = FilesReceiverListenerService.this.createFileReceiver();
                fileReceivingExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        fileReceiver.receive(inputStream);
                    }
                });
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    filesReceiverListener.start();
                } catch (IOException e) {
                    stopSelf();
                }
            }
        }).start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = intent.getParcelableExtra("resultReceiver");

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Receiver listener")
                .setContentText("Listening for incoming files")
                .setSmallIcon(R.drawable.send_icon)
                .build();

        startForeground(1, notification);

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

    private FileReceiverProtocol createFileReceiver() {
        final FileReceiverProtocol fileReceiver = new FileReceiverProtocol(SystemUtils.getDownloadsDirectory());
        final Bundle bundle = new Bundle();
        fileReceiver.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                //TODO: Transfer received in notification
                bundle.putSerializable("transfer", transfer);
                receiver.send(START, bundle);
            }

            @Override
            public void onFailure(Exception e) {
                //TODO: Transfer error in notification
                bundle.putSerializable("exception", e);
                receiver.send(FAILURE, bundle);
            }

            @Override
            public void onProgressUpdated() {
                //TODO: Update progress in notification
                receiver.send(PROGRESS_UPDATED, bundle);
            }

            @Override
            public void onSuccess(File file) {
                notifySystemAboutNewFile(file);
                bundle.putSerializable("file", file);
                receiver.send(SUCCESS, bundle);
            }
        });

        return fileReceiver;
    }

    private void notifySystemAboutNewFile(File file) {
        MediaScannerConnection.scanFile(FilesReceiverListenerService.this,
                new String[]{file.toString()},
                new String[]{file.getName()},
                null);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        filesReceiverListener.stop();
        fileReceivingExecutor.shutdownNow();
        super.onDestroy();
    }
}