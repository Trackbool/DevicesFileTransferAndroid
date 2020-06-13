package com.afa.devicesfiletransfer.domain.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransferFile {
    boolean exists();

    String getName();

    String getPath();

    long length();

    InputStream getInputStream() throws FileNotFoundException;

    OutputStream getOutputStream() throws IOException;
}
