package com.afa.devicesfiletransfer.view.ui.filesender.viewmodel;

import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceInteractor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceLauncher;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SendTransferViewModelFactory implements ViewModelProvider.Factory {
    private final FileSenderServiceLauncher fileSenderServiceLauncher;
    private final FileSenderServiceInteractor fileSenderServiceInteractor;

    public SendTransferViewModelFactory(FileSenderServiceLauncher fileSenderServiceLauncher,
                                        FileSenderServiceInteractor fileSenderServiceInteractor) {
        this.fileSenderServiceLauncher = fileSenderServiceLauncher;
        this.fileSenderServiceInteractor = fileSenderServiceInteractor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SendTransferViewModel(fileSenderServiceLauncher, fileSenderServiceInteractor);
    }
}
