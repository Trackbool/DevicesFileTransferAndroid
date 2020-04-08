package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TransfersViewModelFactory implements ViewModelProvider.Factory {
    private final FilesReceiverListenerServiceExecutor receiverServiceExecutor;
    private final FilesReceiverListenerReceiver receiverListenerReceiver;
    private final FileSenderReceiver fileSenderReceiver;

    public TransfersViewModelFactory(FilesReceiverListenerServiceExecutor receiverServiceExecutor,
                                     FilesReceiverListenerReceiver receiverListenerReceiver,
                                     FileSenderReceiver fileSenderReceiver) {
        this.receiverServiceExecutor = receiverServiceExecutor;
        this.receiverListenerReceiver = receiverListenerReceiver;
        this.fileSenderReceiver = fileSenderReceiver;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TransfersViewModel(
                receiverServiceExecutor, receiverListenerReceiver, fileSenderReceiver);
    }
}