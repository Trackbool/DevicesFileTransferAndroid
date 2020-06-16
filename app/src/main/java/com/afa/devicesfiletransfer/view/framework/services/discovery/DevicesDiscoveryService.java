package com.afa.devicesfiletransfer.view.framework.services.discovery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.afa.devicesfiletransfer.ConfigProperties;
import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListener;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolListenerFactory;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSender;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryProtocolSenderFactory;
import com.afa.devicesfiletransfer.view.framework.WifiMulticastLockManager;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FileReceiverService;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DevicesDiscoveryService extends Service {
    private static final String CHANNEL_ID = FileReceiverService.class.getName() + "Channel";
    private final IBinder binder = new LocalBinder();
    private DiscoveryProtocolSender discoverySender;
    private DiscoveryProtocolListener discoveryListener;
    private List<DiscoveryProtocolListener.Callback> callbackReceivers = new ArrayList<>();

    public class LocalBinder extends Binder {
        DevicesDiscoveryService getService() {
            return DevicesDiscoveryService.this;
        }
    }

    public void addCallbackReceiver(DiscoveryProtocolListener.Callback callbackReceiver) {
        if (!callbackReceivers.contains(callbackReceiver)) {
            callbackReceivers.add(callbackReceiver);
        }
    }

    public void removeCallbackReceiver(DiscoveryProtocolListener.Callback callbackReceiver) {
        callbackReceivers.remove(callbackReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(ConfigProperties.DISCOVERY_SERVICE_PORT);
        discoveryListener = createDiscoveryListener();
        discoveryListener.start();

        WifiMulticastLockManager wifiMulticastLockManager = WifiMulticastLockManager.getInstance();
        if (wifiMulticastLockManager.isWifiEnabled(getApplicationContext())) {
            wifiMulticastLockManager.acquire(getApplicationContext());
        }

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
        return DiscoveryProtocolListenerFactory
                .getDefault(ConfigProperties.DISCOVERY_SERVICE_PORT, new DiscoveryProtocolListener.Callback() {
                    @Override
                    public void initializationFailure(Exception e) {
                        for (DiscoveryProtocolListener.Callback callback : callbackReceivers) {
                            callback.initializationFailure(e);
                        }
                        stopSelf();
                    }

                    @Override
                    public void discoveryRequestReceived(Device device) {
                        for (DiscoveryProtocolListener.Callback callback : callbackReceivers) {
                            callback.discoveryRequestReceived(device);
                        }
                    }

                    @Override
                    public void discoveryResponseReceived(Device device) {
                        for (DiscoveryProtocolListener.Callback callback : callbackReceivers) {
                            callback.discoveryResponseReceived(device);
                        }
                    }

                    @Override
                    public void discoveryDisconnect(Device device) {
                        for (DiscoveryProtocolListener.Callback callback : callbackReceivers) {
                            callback.discoveryDisconnect(device);
                        }
                    }
                });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        noticeDisconnected();
        discoveryListener.stop();
        WifiMulticastLockManager.getInstance().release();
        super.onDestroy();
    }

    private void noticeDisconnected() {
        try {
            discoverySender.noticeDisconnect();
        } catch (SocketException ignored) {
        }
    }
}
