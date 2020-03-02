package com.afa.devicesfiletransfer.view.transfer.receiver;

import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ReceiveTransferPresenter implements ReceiveTransferContract.Presenter {
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private ReceiveTransferContract.View view;
    private final FilesReceiverListenerServiceExecutor receiverServiceExecutor;

    public ReceiveTransferPresenter(ReceiveTransferContract.View view,
                                    FilesReceiverListenerServiceExecutor receiverServiceExecutor) {
        this.view = view;
        this.receiverServiceExecutor = receiverServiceExecutor;
    }

    @Override
    public void onViewLoaded() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiverServiceExecutor.start();
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
        view = null;
    }
}
