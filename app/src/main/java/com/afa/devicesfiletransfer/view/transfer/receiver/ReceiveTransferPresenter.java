package com.afa.devicesfiletransfer.view.transfer.receiver;

import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ReceiveTransferPresenter implements ReceiveTransferContract.Presenter {
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private final ReceiveTransferContract.View view;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileReceivingExecutor;

    public ReceiveTransferPresenter(ReceiveTransferContract.View view) {
        this.view = view;
        fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onViewLoaded() {
        filesReceiverListener = new FilesReceiverListener(TRANSFER_SERVICE_PORT, new FilesReceiverListener.Callback() {
            @Override
            public void onTransferReceived(final InputStream inputStream) {
                final FileReceiverProtocol fileReceiver = ReceiveTransferPresenter.this.createFileReceiver();
                fileReceivingExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        fileReceiver.receive(inputStream);
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    filesReceiverListener.start();
                } catch (IOException e) {
                    view.showError("Initialization error", e.getMessage());
                    view.close();
                }
            }
        }).start();
    }

    private FileReceiverProtocol createFileReceiver() {
        final FileReceiverProtocol fileReceiver = new FileReceiverProtocol(view.getDownloadsDirectory());
        fileReceiver.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart() {
                view.addReceptionTransfer(fileReceiver.getTransfer());
            }

            @Override
            public void onFailure(Exception e) {
                view.refreshReceptionsData();
                view.showError("Receiving error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {
                view.refreshReceptionsData();
            }

            @Override
            public void onSuccess(File file) {
                view.refreshReceptionsData();
                view.showAlert("Receiving success", file.getName());
            }
        });

        return fileReceiver;
    }

    @Override
    public void onDestroy() {
        filesReceiverListener.stop();
        fileReceivingExecutor.shutdownNow();
    }
}
