package com.afa.devicesfiletransfer.model;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface TransferFile {
    String getName();

    String getPath();

    long length();

    InputStream getInputStream() throws FileNotFoundException;
}
