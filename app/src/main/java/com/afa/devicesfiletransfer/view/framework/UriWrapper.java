package com.afa.devicesfiletransfer.view.framework;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;

public class UriWrapper implements Parcelable {
    private Context context;
    private final Uri uri;
    private String fileName;
    private long length;
    private String realPath;

    public UriWrapper(Context context, Uri uri) {
        this.context = context.getApplicationContext();
        this.uri = uri;
        setUpFileName();
        setUpLength();
        setUpRealPath();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLength() {
        return length;
    }

    public String getRealPath() {
        return realPath == null || realPath.isEmpty() ? getFileName() : realPath;
    }

    public Uri getUri() {
        return uri;
    }

    private void setUpLength() {
        try (ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                .openFileDescriptor(uri, "r")) {

            length = fileDescriptor != null ? fileDescriptor.getStatSize() : -1;
        } catch (Exception e) {
            length = -1;
        }
    }

    private void setUpFileName() {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (SecurityException e) {
                result = null;
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        fileName = result;
    }

    private void setUpRealPath() {
        realPath = null;
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    realPath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                realPath = null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean exists() {
        if (uri.getScheme() == null) return false;

        switch (uri.getScheme()) {
            case "content":
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    return documentUriExists(uri);
                } else {
                    return contentUriExists(uri);
                }
            case "file":
                default:
                return new File(uri.getPath()).exists();
        }
    }

    private boolean documentUriExists(Uri uri) {
        return resolveUri(uri, DocumentsContract.Document.COLUMN_DOCUMENT_ID);
    }

    private boolean contentUriExists(Uri uri) {
        return resolveUri(uri, BaseColumns._ID);
    }

    private boolean resolveUri(Uri uri, String column) {
        boolean result = false;

        Cursor cursor;
        try {
            cursor = context.getContentResolver()
                    .query(uri, new String[]{column}, null, null, null);
        } catch (Exception e) {
            return false;
        }

        if (cursor != null) {
            result = cursor.moveToFirst();
            cursor.close();
        }

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(fileName);
        dest.writeLong(length);
        dest.writeString(realPath);
    }

    private UriWrapper(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        fileName = in.readString();
        length = in.readLong();
        realPath = in.readString();
    }

    public static final Creator<UriWrapper> CREATOR = new Creator<UriWrapper>() {
        @Override
        public UriWrapper createFromParcel(Parcel in) {
            return new UriWrapper(in);
        }

        @Override
        public UriWrapper[] newArray(int size) {
            return new UriWrapper[size];
        }
    };
}
