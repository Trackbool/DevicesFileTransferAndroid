package com.afa.devicesfiletransfer.view.viewmodels.transfer.receiver;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ReceiveTransferViewModelFactory implements ViewModelProvider.Factory {
    private FilesReceiverListenerServiceExecutor filesReceiverListenerExecutor;
    private FilesReceiverListenerReceiver filesReceiverListenerReceiver;

    public ReceiveTransferViewModelFactory(FilesReceiverListenerServiceExecutor filesReceiverListenerExecutor,
                                           FilesReceiverListenerReceiver filesReceiverListenerReceiver) {
        this.filesReceiverListenerExecutor = filesReceiverListenerExecutor;
        this.filesReceiverListenerReceiver = filesReceiverListenerReceiver;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ReceiveTransferViewModel(filesReceiverListenerExecutor, filesReceiverListenerReceiver);
    }
}
