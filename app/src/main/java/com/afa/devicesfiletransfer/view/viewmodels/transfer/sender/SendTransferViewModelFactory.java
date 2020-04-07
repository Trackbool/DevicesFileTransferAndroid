package com.afa.devicesfiletransfer.view.viewmodels.transfer.sender;

import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SendTransferViewModelFactory implements ViewModelProvider.Factory {
    private final FileSenderServiceExecutor fileSenderServiceExecutor;
    private final FileSenderReceiver fileSenderReceiver;

    public SendTransferViewModelFactory(FileSenderServiceExecutor fileSenderServiceExecutor,
                                        FileSenderReceiver fileSenderReceiver) {
        this.fileSenderServiceExecutor = fileSenderServiceExecutor;
        this.fileSenderReceiver = fileSenderReceiver;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SendTransferViewModel(fileSenderServiceExecutor, fileSenderReceiver);
    }
}
