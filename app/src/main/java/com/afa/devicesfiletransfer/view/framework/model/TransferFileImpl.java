package com.afa.devicesfiletransfer.view.framework.model;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.view.framework.UriWrapper;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class TransferFileImpl implements TransferFile, Parcelable {
    private Context context;
    private UriWrapper uriWrapper;
    private Uri uri;

    public TransferFileImpl(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        this.uriWrapper = new UriWrapper(context, uri);
    }

    public void setContext(Context context) {
        this.context = context;
        uriWrapper = new UriWrapper(context, uri);
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
            return context.getContentResolver().openInputStream(uriWrapper.getUri());
        } catch (SecurityException e) {
            throw new FileNotFoundException("The file " + getName() + " doesn´t exists");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
    }

    private TransferFileImpl(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<TransferFileImpl> CREATOR = new Creator<TransferFileImpl>() {
        @Override
        public TransferFileImpl createFromParcel(Parcel in) {
            return new TransferFileImpl(in);
        }

        @Override
        public TransferFileImpl[] newArray(int size) {
            return new TransferFileImpl[size];
        }
    };
}
