package com.afa.devicesfiletransfer.view.framework.services.discovery;

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
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.DeviceProperties;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListenerFactory;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerService;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DevicesDiscoveryService extends Service {
    public static final int INITIALIZATION_FAILURE = 0;
    public static final int REQUEST_RECEIVED = 1;
    public static final int RESPONSE_RECEIVED = 2;
    private static final int DISCOVERY_SERVICE_PORT = 5000;
    private static final String CHANNEL_ID = FilesReceiverListenerService.class.getName() + "Channel";
    private final IBinder binder = new LocalBinder();
    private DiscoveryProtocolListener discoveryListener;
    private List<ResultReceiver> receivers = new ArrayList<>();

    public class LocalBinder extends Binder {
        DevicesDiscoveryService getService() {
            return DevicesDiscoveryService.this;
        }
    }

    public void addResultReceiver(ResultReceiver resultReceiver) {
        receivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        receivers.remove(resultReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        discoveryListener = createDiscoveryListener();
        discoveryListener.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Devices discovery")
                .setContentText("Listening")
                .setSmallIcon(R.drawable.icon_send)
                .build();

        startForeground(3, notification);

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

    private DiscoveryProtocolListener createDiscoveryListener() {
        final Bundle bundle = new Bundle();
        return DiscoveryProtocolListenerFactory
                .getDefault(DISCOVERY_SERVICE_PORT, new DiscoveryProtocolListener.Callback() {
                    @Override
                    public void initializationFailure(Exception e) {
                        bundle.putSerializable("exception", e);
                        sendToAllReceivers(INITIALIZATION_FAILURE, bundle);
                        stopSelf();
                    }

                    @Override
                    public void discoveryRequestReceived(Device device) {
                        bundle.putParcelable("device", device);
                        sendToAllReceivers(REQUEST_RECEIVED, bundle);
                    }

                    @Override
                    public void discoveryResponseReceived(Device device) {
                        bundle.putParcelable("device", device);
                        sendToAllReceivers(RESPONSE_RECEIVED, bundle);
                    }
                });
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
        discoveryListener.stop();
        super.onDestroy();
    }
}
