package com.afa.devicesfiletransfer.domain.model;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    public File getFile() {
        return file;
    }

    @NotNull
    @Override
    public String toString() {
        return file.toString();
    }
}
