package com.afa.devicesfiletransfer.services.transfer.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class FilesReceiverListener {
    private ServerSocket serverSocket;
    private final int port;
    private final Callback callback;
    private final AtomicBoolean listening;

    public FilesReceiverListener(int port, Callback callback) {
        this.port = port;
        this.callback = callback;
        this.listening = new AtomicBoolean(false);
    }

    public void start() throws IOException {
        if (listening.get()) throw new IllegalStateException("Listener already listening");

        serverSocket = new ServerSocket(port);
        listening.set(true);
        while (listening.get()) {
            try {
                Socket socket = serverSocket.accept();
                try {
                    callback.onTransferReceived(socket.getInputStream());
                } catch (IOException ignored) {
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void stop() {
        listening.set(false);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public interface Callback {
        void onTransferReceived(InputStream inputStream);
    }
}
