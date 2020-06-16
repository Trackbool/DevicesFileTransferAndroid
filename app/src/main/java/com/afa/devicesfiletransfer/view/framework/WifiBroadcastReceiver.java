package com.afa.devicesfiletransfer.view.framework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            WifiMulticastLockManager wifiMulticastLockManager = WifiMulticastLockManager.getInstance();
            if (wifiMulticastLockManager.isWifiEnabled(context)) {
                wifiMulticastLockManager.acquire(context);
            } else {
                wifiMulticastLockManager.release();
            }
        }
    }
}
