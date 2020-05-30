package com.afa.devicesfiletransfer.domain.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.afa.devicesfiletransfer.framework.TransferFileUri;

import java.io.File;

public class TransferFileFactory {
    public static TransferFile getFromFile(File file) {
        return new TransferFileLocal(file);
    }

    public static TransferFile getFromUri(Uri uri) {
        return new TransferFileUri(uri);
    }

    public static TransferFile getFromPath(String filePath) {
        if (filePath.startsWith(ContentResolver.SCHEME_CONTENT)) {
            return getFromUri(Uri.parse(filePath));
        } else {
            return getFromFile(new File(filePath));
        }
    }

    public static Uri getUriFromTransferFile(TransferFile transferFile) {
        Uri fileUri;
        if (transferFile instanceof TransferFileUri) {
            fileUri = ((TransferFileUri) transferFile).getUri();
        } else {
            File file = ((TransferFileLocal) transferFile).getFile();
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }
}