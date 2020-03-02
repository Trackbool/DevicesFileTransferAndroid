package com.afa.devicesfiletransfer.view.framework.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;
import com.afa.devicesfiletransfer.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FilesReceiverListenerService extends Service {
    public static final String CHANNEL_ID = FilesReceiverListenerService.class.getName() + "Channel";
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
        fileReceiver.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart() {
                //Notify transfer received
            }

            @Override
            public void onFailure(Exception e) {
                //Transfer error
            }

            @Override
            public void onProgressUpdated() {

            }

            @Override
            public void onSuccess(File file) {
                //Transfer succeeded
            }
        });

        return fileReceiver;
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