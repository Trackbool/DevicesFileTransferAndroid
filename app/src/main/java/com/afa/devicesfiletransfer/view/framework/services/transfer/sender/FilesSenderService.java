package com.afa.devicesfiletransfer.view.framework.services.transfer.sender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.framework.TransferFileUri;
import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.usecases.SaveTransferUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FilesSenderService extends Service {
    private static final String CHANNEL_ID = FilesSenderService.class.getName() + "Channel";
    private ThreadPoolExecutor fileSendingExecutor;
    private SaveTransferUseCase saveTransferUseCase;
    private final IBinder binder = new FilesSenderService.LocalBinder();
    private List<FileSenderProtocol.Callback> callbackReceivers = new ArrayList<>();
    private List<Transfer> inProgressTransfers = new ArrayList<>();

    public class LocalBinder extends Binder {
        FilesSenderService getService() {
            return FilesSenderService.this;
        }
    }

    public FilesSenderService() {
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public void addCallbackReceiver(FileSenderProtocol.Callback callbackReceiver) {
        if (!callbackReceivers.contains(callbackReceiver)) {
            callbackReceivers.add(callbackReceiver);
        }
    }

    public void removeCallbackReceiver(FileSenderProtocol.Callback callbackReceiver) {
        callbackReceivers.remove(callbackReceiver);
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

        final List<TransferFileUri> intentFiles = intent.getParcelableArrayListExtra("files");
        final List<TransferFile> files = new ArrayList<>();
        for (final TransferFileUri file : intentFiles) {
            file.setContext(getApplicationContext());
            files.add(file);
        }

        for (final Device device : devices) {
            fileSendingExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final FileSenderProtocol fileSenderProtocol = createFileSender(device, files);
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

    private FileSenderProtocol createFileSender(Device device, List<TransferFile> files) {
        FileSenderProtocol fileSender = new FileSenderProtocol(device, files);
        fileSender.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onInitializationFailure() {
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onInitializationFailure();
                    }
                }
            }

            @Override
            public void onTransferInitializationFailure(Transfer transfer, Exception e) {
                inProgressTransfers.remove(transfer);
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onTransferInitializationFailure(transfer, e);
                    }
                }
            }

            @Override
            public void onStart(Transfer transfer) {
                //TODO: Notify the file is sending
                inProgressTransfers.add(transfer);
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onStart(transfer);
                    }
                }
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                //TODO: Notify failure in transfer
                inProgressTransfers.remove(transfer);
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onFailure(transfer, e);
                    }
                }
                persistTransfer(transfer);
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                //TODO: Update the progress in notification
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onProgressUpdated(transfer);
                    }
                }
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                //TODO: Notify transfer succeeded
                inProgressTransfers.remove(transfer);
                for (FileSenderProtocol.Callback callback : callbackReceivers) {
                    if (callback != null) {
                        callback.onSuccess(transfer, file);
                    }
                }
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