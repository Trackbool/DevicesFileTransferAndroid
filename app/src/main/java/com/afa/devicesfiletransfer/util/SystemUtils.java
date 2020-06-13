package com.afa.devicesfiletransfer.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.afa.devicesfiletransfer.DftApplication;

import java.io.File;

public class SystemUtils {
    public static String getSystemName() {
        String name = android.os.Build.MODEL;
        if (name != null && name.length() > 0) return name;

        return "Unknown";
    }

    public static String getOs() {
        return "Android " + Build.VERSION.RELEASE;
    }

    public static File getAppDownloadsDirectory() {
        String appFolderName = "DevicesFileTransfer";
        Context context = DftApplication.getContext();
        File f;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            f = context.getExternalFilesDir("ReceivedFiles");
        } else {
            f = new File(Environment.getExternalStorageDirectory(), appFolderName);
            if (!f.exists()) {
                f.mkdirs();
            }
        }

        return f;
    }
}
