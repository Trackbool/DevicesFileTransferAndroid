package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import android.content.Context;

import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.usecases.GetLastTransfersUseCase;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerReceiverImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderReceiverImpl;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TransfersViewModelFactory implements ViewModelProvider.Factory {
    private final GetLastTransfersUseCase getLastTransfersUseCase;
    private final FilesReceiverListenerServiceExecutor receiverServiceExecutor;
    private final FilesReceiverListenerReceiver filesReceiverListenerReceiver;
    private final FileSenderReceiver fileSenderReceiver;

    public TransfersViewModelFactory(Context context) {
        getLastTransfersUseCase = new GetLastTransfersUseCase(new TransfersRoomDatabaseRepository(context));
        receiverServiceExecutor = new FilesReceiverListenerServiceExecutorImpl(context);
        filesReceiverListenerReceiver = new FilesReceiverListenerReceiverImpl(context);
        fileSenderReceiver = new FileSenderReceiverImpl(context);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TransfersViewModel(getLastTransfersUseCase,
                receiverServiceExecutor, filesReceiverListenerReceiver, fileSenderReceiver);
    }
}