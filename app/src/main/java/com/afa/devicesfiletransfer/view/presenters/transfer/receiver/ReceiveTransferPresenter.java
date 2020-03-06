package com.afa.devicesfiletransfer.view.presenters.transfer.receiver;

import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

import java.io.File;

public class ReceiveTransferPresenter implements ReceiveTransferContract.Presenter {
    private ReceiveTransferContract.View view;
    private final FilesReceiverListenerServiceExecutor receiverServiceExecutor;

    public ReceiveTransferPresenter(final ReceiveTransferContract.View view,
                                    FilesReceiverListenerServiceExecutor receiverServiceExecutor) {
        this.view = view;
        this.receiverServiceExecutor = receiverServiceExecutor;
        receiverServiceExecutor.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                view.addReceptionTransfer(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                view.showError("Receiving error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {
                view.refreshReceptionsData();
            }

            @Override
            public void onSuccess(File file) {
                view.showAlert("Receiving success", file.getName());
            }
        });
    }

    @Override
    public void onViewLoaded() {
        receiverServiceExecutor.start();
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
