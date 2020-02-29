package com.afa.devicesfiletransfer.util;

public class SystemUtils {
    public static String getSystemName() {
        String name = android.os.Build.MODEL;
        if(name != null && name.length() > 0) return name;

        return "Unknown";
    }

    public static String getOs() {
        return "Android";
    }
}
