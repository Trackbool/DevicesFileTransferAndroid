package com.afa.devicesfiletransfer.view.framework;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiMulticastLockManager {
    private static final String WIFI_LOCK_TAG = "WIFI_LOCK_TAG";
    private static WifiMulticastLockManager instance;
    private WifiManager.MulticastLock lock;

    private WifiMulticastLockManager() {}

    public static WifiMulticastLockManager getInstance() {
        if (instance == null) {
            instance = new WifiMulticastLockManager();
        }

        return instance;
    }

    public boolean isWifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        return wifi != null && wifi.isWifiEnabled();
    }

    public boolean isLockHeld() {
        return lock != null && lock.isHeld();
    }

    public void acquire(Context context) {
        if (isLockHeld()) {
            return;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null || !wifi.isWifiEnabled()) {
            throw new WifiNotConnectedException("Check if WiFi is enabled before acquiring the lock");
        }
        lock = wifi.createMulticastLock(WIFI_LOCK_TAG);
        lock.acquire();
    }

    public void release() {
        if (isLockHeld()) {
            lock.release();
        }
    }
}
