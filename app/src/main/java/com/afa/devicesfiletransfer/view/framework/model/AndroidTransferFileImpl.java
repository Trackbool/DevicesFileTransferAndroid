package com.afa.devicesfiletransfer.view.framework.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;

import com.afa.devicesfiletransfer.model.TransferFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AndroidTransferFileImpl implements TransferFile, Parcelable {
    private Context context;
    private final Uri uri;
    private final File file;
    private long length;

    public AndroidTransferFileImpl(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        file = new File(uri.getPath());
        calculateLength();
    }

    private void calculateLength() {
        try (ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                .openFileDescriptor(uri, "r")) {

            length = fileDescriptor != null ? fileDescriptor.getStatSize() : -1;
        } catch (IOException e) {
            length = -1;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uri);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeSerializable(file);
        dest.writeLong(length);
    }

    private AndroidTransferFileImpl(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        file = (File) in.readSerializable();
        length = in.readLong();
    }

    public static final Creator<AndroidTransferFileImpl> CREATOR = new Creator<AndroidTransferFileImpl>() {
        @Override
        public AndroidTransferFileImpl createFromParcel(Parcel in) {
            return new AndroidTransferFileImpl(in);
        }

        @Override
        public AndroidTransferFileImpl[] newArray(int size) {
            return new AndroidTransferFileImpl[size];
        }
    };
}
