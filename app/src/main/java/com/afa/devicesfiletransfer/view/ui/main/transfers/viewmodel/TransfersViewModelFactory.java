package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import android.content.Context;

import com.afa.devicesfiletransfer.framework.repository.TransfersRoomDatabaseRepository;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverServiceInteractor;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverServiceLauncher;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceInteractor;
import com.afa.devicesfiletransfer.usecases.GetLastTransfersUseCase;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FileReceiverServiceInteractorImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FileReceiverServiceLauncherImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderServiceInteractorImpl;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TransfersViewModelFactory implements ViewModelProvider.Factory {
    private final GetLastTransfersUseCase getLastTransfersUseCase;
    private final FileReceiverServiceLauncher receiverServiceExecutor;
    private final FileReceiverServiceInteractor fileReceiverServiceInteractor;
    private final FileSenderServiceInteractor fileSenderServiceInteractor;

    public TransfersViewModelFactory(Context context) {
        getLastTransfersUseCase = new GetLastTransfersUseCase(new TransfersRoomDatabaseRepository(context));
        receiverServiceExecutor = new FileReceiverServiceLauncherImpl(context);
        fileReceiverServiceInteractor = new FileReceiverServiceInteractorImpl(context);
        fileSenderServiceInteractor = new FileSenderServiceInteractorImpl(context);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TransfersViewModel(getLastTransfersUseCase,
                receiverServiceExecutor, fileReceiverServiceInteractor, fileSenderServiceInteractor);
    }
}