package com.afa.devicesfiletransfer.framework;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.afa.devicesfiletransfer.DftApplication;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.domain.model.TransferFileLocal;
import com.afa.devicesfiletransfer.framework.TransferFileUri;
import com.afa.devicesfiletransfer.util.SystemUtils;
import com.afa.devicesfiletransfer.util.file.FileUtils;

import java.io.File;

import androidx.core.content.FileProvider;

public class TransferFileFactory {
    public static TransferFile getFromFile(File file) {
        return new TransferFileLocal(file);
    }

    public static TransferFile getFromUri(Uri uri) {
        return new TransferFileUri(uri);
    }

    public static TransferFile getFromUri(Uri uri, String fileName) {
        return new TransferFileUri(uri, fileName);
    }

    public static TransferFile getFromPath(String filePath) {
        return getFromPath(filePath, null);
    }

    public static TransferFile getFromPath(String filePath, String fileName) {
        if (filePath.startsWith(ContentResolver.SCHEME_CONTENT)) {
            if (fileName != null) {
                return getFromUri(Uri.parse(filePath), fileName);
            } else {
                return getFromUri(Uri.parse(filePath));
            }
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
            fileUri = FileProvider.getUriForFile(
                    DftApplication.getContext(),
                    DftApplication.getContext()
                            .getPackageName() + ".provider", file);
        }
        return fileUri;
    }

    /**
     * Creates an incoming transfer file. It also creates a reference in fileSystem from Android Q.
     * @param fileName the fileName
     * @return the new file Uri
     */
    public static TransferFile createIncomingTransferFile(String fileName) {
        Context context = DftApplication.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);

            String extension = FileUtils.getFileExtension(fileName);
            String mime = "application/octet-stream";
            if (extension != null && !extension.equals("")) {
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mime);

            String volume = MediaStore.VOLUME_EXTERNAL_PRIMARY;
            String directory = Environment.DIRECTORY_DOWNLOADS;
            Uri externalContentUri = MediaStore.Downloads.getContentUri(volume);
            if (FileUtils.isImage(fileName)) {
                directory = Environment.DIRECTORY_PICTURES;
                externalContentUri = MediaStore.Images.Media.getContentUri(volume);
            } else if (FileUtils.isVideo(fileName)) {
                directory = Environment.DIRECTORY_MOVIES;
                externalContentUri = MediaStore.Video.Media.getContentUri(volume);
            } else if (FileUtils.isAudio(fileName)) {
                directory = Environment.DIRECTORY_MUSIC;
                externalContentUri = MediaStore.Audio.Media.getContentUri(volume);
            }
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directory);
            ContentResolver resolver = context.getContentResolver();
            Uri fileUri = resolver.insert(externalContentUri, contentValues);
            return getFromUri(fileUri);
        } else {
            File file = new File(SystemUtils.getAppDownloadsDirectory(), fileName);
            return getFromFile(file);
        }
    }
}