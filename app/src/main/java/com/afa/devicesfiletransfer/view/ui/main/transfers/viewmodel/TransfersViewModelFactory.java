package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import android.content.Context;

import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverInteractor;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverServiceExecutor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderInteractor;
import com.afa.devicesfiletransfer.usecases.GetLastTransfersUseCase;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverInteractorImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderInteractorImpl;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TransfersViewModelFactory implements ViewModelProvider.Factory {
    private final GetLastTransfersUseCase getLastTransfersUseCase;
    private final FilesReceiverServiceExecutor receiverServiceExecutor;
    private final FilesReceiverInteractor filesReceiverInteractor;
    private final FileSenderInteractor fileSenderInteractor;

    public TransfersViewModelFactory(Context context) {
        getLastTransfersUseCase = new GetLastTransfersUseCase(new TransfersRoomDatabaseRepository(context));
        receiverServiceExecutor = new FilesReceiverServiceExecutorImpl(context);
        filesReceiverInteractor = new FilesReceiverInteractorImpl(context);
        fileSenderInteractor = new FileSenderInteractorImpl(context);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TransfersViewModel(getLastTransfersUseCase,
                receiverServiceExecutor, filesReceiverInteractor, fileSenderInteractor);
    }
}