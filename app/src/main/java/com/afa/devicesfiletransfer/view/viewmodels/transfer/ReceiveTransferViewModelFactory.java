package com.afa.devicesfiletransfer.view.viewmodels.transfer;

import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ReceiveTransferViewModelFactory implements ViewModelProvider.Factory {
    private FilesReceiverListenerServiceExecutor filesReceiverListenerExecutor;

    public ReceiveTransferViewModelFactory(FilesReceiverListenerServiceExecutor filesReceiverListenerExecutor) {
        this.filesReceiverListenerExecutor = filesReceiverListenerExecutor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ReceiveTransferViewModel(filesReceiverListenerExecutor);
    }
}
