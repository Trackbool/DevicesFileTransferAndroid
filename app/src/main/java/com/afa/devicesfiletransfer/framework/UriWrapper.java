package com.afa.devicesfiletransfer.framework;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

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
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getFileName() {
        if (fileName == null)
            setUpFileName();

        return fileName;
    }

    public long getLength() {
        if (length < 1)
            setUpLength();

        return length;
    }

    public String getRealPath() {
        realPath = uri.toString();

        if (!realPath.isEmpty()) {
            return realPath;
        }
        if (getFileName() != null) {
            return fileName;
        }

        return uri.getPath();
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
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.DISPLAY_NAME},
                            null, null, null)) {
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

        if (!hasExtension(result)) {
            String extension = getExtension();
            if (extension != null && !extension.equals("")) {
                result = result + "." + extension;
            }
        }

        fileName = result;
    }

    private boolean hasExtension(String fileName) {
        return fileName != null && fileName.contains(".");
    }

    public String getExtension() {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public boolean exists() {
        if (uri.getScheme() == null) return false;

        switch (uri.getScheme()) {
            case ContentResolver.SCHEME_CONTENT:
                try (Cursor cursor = context.getContentResolver()
                        .query(uri, null, null, null, null)) {
                    return (cursor != null && cursor.getCount() > 0) && getLength() > -1;
                } catch (Exception e) {
                    return false;
                }
            case ContentResolver.SCHEME_FILE:
            default:
                return new File(uri.getPath()).exists();
        }
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
