package com.afa.devicesfiletransfer.framework;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.afa.devicesfiletransfer.DftApplication;
import com.afa.devicesfiletransfer.domain.model.TransferFile;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TransferFileUri implements TransferFile, Parcelable {
    private UriWrapper uriWrapper;
    private Uri uri;
    private String displayName;

    public TransferFileUri(Uri uri) {
        this.uri = uri;
        this.uriWrapper = new UriWrapper(DftApplication.getContext(), uri);
    }

    public TransferFileUri(Uri uri, String displayName) {
        this(uri);
        this.displayName = displayName;
    }

    @Override
    public boolean exists() {
        return uriWrapper.exists();
    }

    @Override
    public String getName() {
        if (displayName != null) {
            return displayName;
        }

        return uriWrapper.getFileName();
    }

    @Override
    public String getPath() {
        return uriWrapper.getRealPath();
    }

    @Override
    public long length() {
        return uriWrapper.getLength();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        try {
            return DftApplication.getContext().getContentResolver()
                    .openInputStream(uri);
        } catch (SecurityException e) {
            throw new FileNotFoundException("The file " + getName() + " doesn´t exists");
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            return DftApplication.getContext().getContentResolver()
                    .openOutputStream(uri);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public Uri getUri() {
        return uri;
    }

    @NotNull
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeString(displayName);
    }

    private TransferFileUri(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        uriWrapper = new UriWrapper(DftApplication.getContext(), uri);
        displayName = in.readString();
    }

    public static final Creator<TransferFileUri> CREATOR = new Creator<TransferFileUri>() {
        @Override
        public TransferFileUri createFromParcel(Parcel in) {
            return new TransferFileUri(in);
        }

        @Override
        public TransferFileUri[] newArray(int size) {
            return new TransferFileUri[size];
        }
    };
}
