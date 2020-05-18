package com.afa.devicesfiletransfer.view.viewmodels.transfer.sender;

import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderInteractor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SendTransferViewModelFactory implements ViewModelProvider.Factory {
    private final FileSenderServiceExecutor fileSenderServiceExecutor;
    private final FileSenderInteractor fileSenderInteractor;

    public SendTransferViewModelFactory(FileSenderServiceExecutor fileSenderServiceExecutor,
                                        FileSenderInteractor fileSenderInteractor) {
        this.fileSenderServiceExecutor = fileSenderServiceExecutor;
        this.fileSenderInteractor = fileSenderInteractor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SendTransferViewModel(fileSenderServiceExecutor, fileSenderInteractor);
    }
}
