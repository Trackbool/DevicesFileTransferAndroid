package com.afa.devicesfiletransfer.view.presenters.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

public class SendTransferPresenter implements SendTransferContract.Presenter {
    private SendTransferContract.View view;
    private final FileSenderServiceExecutor fileSenderExecutor;
    private TransferFile fileToSend;

    public SendTransferPresenter(final SendTransferContract.View view, FileSenderServiceExecutor fileSenderExecutor) {
        this.view = view;
        this.fileSenderExecutor = fileSenderExecutor;
        fileSenderExecutor.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                view.addSendingTransfer(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                view.showError("Sending error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {
                view.refreshSendingData();
            }

            @Override
            public void onSuccess(TransferFile file) {
                view.showAlert("Sending success", file.getName());
            }
        });
    }

    @Override
    public void onBrowseFileButtonClicked() {
        view.browseFile();
    }

    @Override
    public void onFileAttached(TransferFile file) {
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
