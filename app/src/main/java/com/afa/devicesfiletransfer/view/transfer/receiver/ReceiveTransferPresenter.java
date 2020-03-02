package com.afa.devicesfiletransfer.view.transfer.receiver;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

//TODO: Put callback in receiverService
public class ReceiveTransferPresenter implements ReceiveTransferContract.Presenter {
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

    @Override
    public void onDestroy() {
        view = null;
    }
}
