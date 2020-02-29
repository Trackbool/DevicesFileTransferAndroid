package com.afa.devicesfiletransfer.view.transfer;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListener;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TransferPresenter implements TransferContract.Presenter {
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private TransferContract.View view;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileSendingExecutor;
    private ThreadPoolExecutor fileReceivingExecutor;

    private File fileToSend;

    public TransferPresenter(TransferContract.View view) {
        this.view = view;
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onViewLoaded() {
        filesReceiverListener = new FilesReceiverListener(TRANSFER_SERVICE_PORT, new FilesReceiverListener.Callback() {
            @Override
            public void onTransferReceived(final InputStream inputStream) {
                final FileReceiverProtocol fileReceiver = createFileReceiver();
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

    @Override
    public void onBrowseFileButtonClicked() {
        view.browseFile();
    }

    @Override
    public void onFileAttached(File file) {
        fileToSend = file;
        view.showFileAttachedName(fileToSend.getName());
    }

    @Override
    public void onSendFileButtonClicked() {
        if (fileToSend == null) {
            view.showAlert("No file attached", "You must attach a file");
            return;
        }

        Device[] devices = view.getSelectedDevices();
        if (devices.length == 0) {
            view.showAlert("No device selected", "You must select one or more devices to send the file");
        }

        for (Device device : devices) {
            sendFile(device);
        }
    }

    private void sendFile(final Device device) {
        fileSendingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileSenderProtocol fileSender = createFileSender(device, fileToSend);
                view.addSendingTransfer(fileSender.getTransfer());
                fileSender.send();
            }
        });
    }

    private FileReceiverProtocol createFileReceiver() {
        final FileReceiverProtocol fileReceiver = new FileReceiverProtocol();
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

    private FileSenderProtocol createFileSender(Device device, File file) {
        FileSenderProtocol fileSender = new FileSenderProtocol(device, file);
        fileSender.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onStart() {
                //The transfer is added to the list when button is clicked, not when starts
            }

            @Override
            public void onFailure(Exception e) {
                view.refreshSendingData();
                view.showError("Sending error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {
                view.refreshSendingData();
            }

            @Override
            public void onSuccess(File file) {
                view.refreshSendingData();
                view.showAlert("Sending success", file.getName());
            }
        });

        return fileSender;
    }

    @Override
    public void onDestroy() {
        filesReceiverListener.stop();
        fileSendingExecutor.shutdownNow();
        fileReceivingExecutor.shutdownNow();
        view = null;
        fileToSend = null;
    }
}
