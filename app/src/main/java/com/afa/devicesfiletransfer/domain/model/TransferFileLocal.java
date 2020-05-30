package com.afa.devicesfiletransfer.domain.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TransferFileLocal implements TransferFile {
    private final File file;

    public TransferFileLocal(File file) {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return file.exists();
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
        return file.length();
    }

    public File getFile() {
        return file;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
