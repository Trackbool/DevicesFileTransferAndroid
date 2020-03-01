package com.afa.devicesfiletransfer.view.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SendTransferPresenter implements SendTransferContract.Presenter {
    private SendTransferContract.View view;
    private ThreadPoolExecutor fileSendingExecutor;

    private File fileToSend;

    public SendTransferPresenter(SendTransferContract.View view) {
        this.view = view;
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
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
        fileSendingExecutor.shutdownNow();
        view = null;
        fileToSend = null;
    }
}
