package com.afa.devicesfiletransfer.model;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface TransferFile {
    boolean exists();

    String getName();

    String getPath();

    long length();

    InputStream getInputStream() throws FileNotFoundException;
}
