package com.afa.devicesfiletransfer.util;

import android.os.Build;
import android.os.Environment;

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

    public static File getDownloadsDirectory() {
        String appFolderName = "DevicesFileTransfer";

        File f = new File(Environment.getExternalStorageDirectory(), appFolderName);
        if (!f.exists()) {
            f.mkdirs();
        }

        return f;
    }
}
