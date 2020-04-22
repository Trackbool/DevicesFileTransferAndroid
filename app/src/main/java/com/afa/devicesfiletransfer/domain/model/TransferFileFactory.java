package com.afa.devicesfiletransfer.domain.model;

import android.content.Context;
import android.net.Uri;

import com.afa.devicesfiletransfer.framework.TransferFileUri;

import java.io.File;

public class TransferFileFactory {
    public static TransferFile getFromFile(File file) {
        return new TransferFileLocal(file);
    }

    public static TransferFile getFromUri(Context context, Uri uri) {
        return new TransferFileUri(context, uri);
    }
}
