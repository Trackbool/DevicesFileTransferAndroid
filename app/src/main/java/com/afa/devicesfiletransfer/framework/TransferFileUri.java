package com.afa.devicesfiletransfer.framework;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.afa.devicesfiletransfer.DftApplication;
import com.afa.devicesfiletransfer.domain.model.TransferFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class TransferFileUri implements TransferFile, Parcelable {
    private UriWrapper uriWrapper;
    private Uri uri;

    public TransferFileUri(Uri uri) {
        this.uri = uri;
        this.uriWrapper = new UriWrapper(DftApplication.getContext(), uri);
    }

    @Override
    public boolean exists() {
        return uriWrapper.exists();
    }

    @Override
    public String getName() {
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
                    .openInputStream(uriWrapper.getUri());
        } catch (SecurityException e) {
            throw new FileNotFoundException("The file " + getName() + " doesn´t exists");
        }
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
    }

    private TransferFileUri(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        uriWrapper = new UriWrapper(DftApplication.getContext(), uri);
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
