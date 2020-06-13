package com.afa.devicesfiletransfer.framework;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import com.afa.devicesfiletransfer.DftApplication;
import com.afa.devicesfiletransfer.domain.model.TransferFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.documentfile.provider.DocumentFile;

public class TransferFileUri implements TransferFile, Parcelable {
    private DocumentFile uriWrapper;
    private Uri uri;

    public TransferFileUri(Uri uri) {
        this.uri = uri;
        this.uriWrapper = DocumentFile.fromSingleUri(DftApplication.getContext(), uri);
    }

    @Override
    public boolean exists() {
        return uriWrapper.exists();
    }

    @Override
    public String getName() {
        String fileName = uriWrapper.getName();
        if (!hasExtension(fileName)) {
            String extension = getExtension();
            if (extension != null && !extension.equals("")) {
                fileName = fileName + "." + extension;
            }
        }

        return fileName;
    }

    private boolean hasExtension(String fileName) {
        return fileName != null && fileName.contains(".");
    }

    public String getExtension() {
        ContentResolver contentResolver = DftApplication.getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public String getPath() {
        return uri.toString();
    }

    @Override
    public long length() {
        return uriWrapper.length();
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
        uriWrapper = DocumentFile.fromSingleUri(DftApplication.getContext(), uri);
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
