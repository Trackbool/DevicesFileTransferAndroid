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
import android.util.Log;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.usecases.SaveTransferUseCase;
import com.afa.devicesfiletransfer.framework.TransferFileUri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesSenderService extends Service {
    public static final int INITIALIZATION_FAILURE = 0;
    public static final int START = 1;
    public static final int FAILURE = 2;
    public static final int PROGRESS_UPDATED = 3;
    public static final int SUCCESS = 4;
    private static final String CHANNEL_ID = FilesSenderService.class.getName() + "Channel";
    private ThreadPoolExecutor fileSendingExecutor;
    private SaveTransferUseCase saveTransferUseCase;
    private final IBinder binder = new FilesSenderService.LocalBinder();
    private List<ResultReceiver> receivers = new ArrayList<>();
    private List<Transfer> inProgressTransfers = new ArrayList<>();

    public class LocalBinder extends Binder {
        FilesSenderService getService() {
            return FilesSenderService.this;
        }
    }

    public FilesSenderService() {
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
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
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final List<Device> devices = intent.getParcelableArrayListExtra("devices");
        final TransferFileUri file = intent.getParcelableExtra("file");
        file.setContext(getApplicationContext());

        for (final Device device : devices) {
            fileSendingExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final FileSenderProtocol fileSenderProtocol = createFileSender(device, file);
                    fileSenderProtocol.send();
                }
            });
        }

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
            public void onInitializationFailure(Transfer transfer, Exception e) {
                bundle.putSerializable("transfer", transfer);
                bundle.putSerializable("exception", e);
                sendToAllReceivers(FAILURE, bundle);
            }

            @Override
            public void onStart(Transfer transfer) {
                //TODO: Notify the file is sending
                inProgressTransfers.add(transfer);
                bundle.putSerializable("transfer", transfer);
                sendToAllReceivers(START, bundle);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                //TODO: Notify failure in transfer
                inProgressTransfers.remove(transfer);
                bundle.putSerializable("transfer", transfer);
                bundle.putSerializable("exception", e);
                sendToAllReceivers(FAILURE, bundle);
                persistTransfer(transfer);
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
                inProgressTransfers.remove(transfer);
                bundle.putSerializable("transfer", transfer);
                bundle.putParcelable("file", (TransferFileUri) file);
                sendToAllReceivers(SUCCESS, bundle);
                persistTransfer(transfer);
            }
        });

        return fileSender;
    }

    private void persistTransfer(final Transfer transfer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveTransferUseCase.execute(transfer, new SaveTransferUseCase.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("Transfers", "Received Transfer persisted");
                        finishServiceIfThereAreNoMoreTransfers();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("Transfers", "Could not persist received transfer: " + e.getMessage());
                        finishServiceIfThereAreNoMoreTransfers();
                    }
                });
            }
        }).start();
    }

    private void finishServiceIfThereAreNoMoreTransfers() {
        if (inProgressTransfers.size() == 0) {
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