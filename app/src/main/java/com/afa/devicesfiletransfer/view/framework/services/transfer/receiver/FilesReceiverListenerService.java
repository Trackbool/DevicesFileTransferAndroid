package com.afa.devicesfiletransfer.view.framework.services.transfer.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;
import com.afa.devicesfiletransfer.usecases.SaveTransferUseCase;
import com.afa.devicesfiletransfer.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesReceiverListenerService extends Service {
    public static final int START = 0;
    public static final int FAILURE = 1;
    public static final int PROGRESS_UPDATED = 2;
    public static final int SUCCESS = 3;
    private static final String CHANNEL_ID = FilesReceiverListenerService.class.getName() + "Channel";
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private SaveTransferUseCase saveTransferUseCase;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileReceivingExecutor;
    private final IBinder binder = new FilesReceiverListenerService.LocalBinder();
    private List<ResultReceiver> receivers = new ArrayList<>();
    private List<Transfer> inProgressTransfers = new ArrayList<>();

    public class LocalBinder extends Binder {
        FilesReceiverListenerService getService() {
            return FilesReceiverListenerService.this;
        }
    }

    public FilesReceiverListenerService() {
        fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public void addResultReceiver(ResultReceiver resultReceiver) {
        receivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        receivers.remove(resultReceiver);
    }

    public List<Transfer> getInProgressTransfers() {
        return Collections.unmodifiableList(inProgressTransfers);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        saveTransferUseCase = new SaveTransferUseCase(
                new TransfersRoomDatabaseRepository(getApplicationContext()));

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
                .setSmallIcon(R.drawable.icon_send)
                .build();

        startForeground(1, notification);

        return START_STICKY;
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
            public void onInitializationFailure() {

            }

            @Override
            public void onStart(Transfer transfer) {
                //TODO: Transfer received in notification
                inProgressTransfers.add(transfer);
                bundle.putSerializable("transfer", transfer);
                sendToAllReceivers(START, bundle);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                //TODO: Transfer error in notification
                inProgressTransfers.remove(transfer);
                bundle.putSerializable("transfer", transfer);
                bundle.putSerializable("exception", e);
                sendToAllReceivers(FAILURE, bundle);
                persistTransfer(transfer);
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                //TODO: Update progress in notification
                bundle.putSerializable("transfer", transfer);
                sendToAllReceivers(PROGRESS_UPDATED, bundle);
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                notifySystemAboutNewFile(file);
                inProgressTransfers.remove(transfer);
                bundle.putSerializable("transfer", transfer);
                bundle.putSerializable("file", file);
                sendToAllReceivers(SUCCESS, bundle);
                persistTransfer(transfer);
            }
        });

        return fileReceiver;
    }

    private void persistTransfer(final Transfer transfer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveTransferUseCase.execute(transfer, new SaveTransferUseCase.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("Transfers", "Received Transfer persisted");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("Transfers", "Could not persist received transfer: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private void notifySystemAboutNewFile(File file) {
        MediaScannerConnection.scanFile(FilesReceiverListenerService.this,
                new String[]{file.toString()},
                new String[]{file.getName()},
                null);
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
        filesReceiverListener.stop();
        fileReceivingExecutor.shutdownNow();
        super.onDestroy();
    }
}
