package com.afa.devicesfiletransfer.view.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

import java.io.File;

public class SendTransferPresenter implements SendTransferContract.Presenter {
    private SendTransferContract.View view;
    private final FileSenderServiceExecutor fileSenderExecutor;
    private File fileToSend;

    public SendTransferPresenter(SendTransferContract.View view, FileSenderServiceExecutor fileSenderExecutor) {
        this.view = view;
        this.fileSenderExecutor = fileSenderExecutor;
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
        fileSenderExecutor.send(device, fileToSend);
    }

    @Override
    public void onDestroy() {
        view = null;
        fileToSend = null;
    }
}
