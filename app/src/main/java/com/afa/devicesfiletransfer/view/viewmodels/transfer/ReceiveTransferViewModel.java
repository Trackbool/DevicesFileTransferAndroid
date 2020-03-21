package com.afa.devicesfiletransfer.view.viewmodels.transfer;

import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReceiveTransferViewModel extends ViewModel {
    private final List<Transfer> transfers;
    private final MutableLiveData<List<Transfer>> transferLiveData;
    private final MutableLiveData<Void> onProgressUpdatedEvent;
    private final MutableLiveData<File> onSuccessEvent;
    private final LiveEvent<ErrorModel> errorEvent;

    public ReceiveTransferViewModel(FilesReceiverListenerServiceExecutor receiverServiceExecutor) {
        transfers = new ArrayList<>();
        transferLiveData = new MutableLiveData<>();
        onProgressUpdatedEvent = new MutableLiveData<>();
        onSuccessEvent = new MutableLiveData<>();
        errorEvent = new LiveEvent<>();
        receiverServiceExecutor.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transferLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Exception e) {
                errorEvent.postValue(new ErrorModel("Receiving error", e.getMessage()));
            }

            @Override
            public void onProgressUpdated() {
                onProgressUpdatedEvent.postValue(null);
            }

            @Override
            public void onSuccess(File file) {
                onSuccessEvent.postValue(file);
            }
        });
        receiverServiceExecutor.start();
    }

    public MutableLiveData<List<Transfer>> getTransferLiveData() {
        return transferLiveData;
    }

    public MutableLiveData<Void> getOnProgressUpdatedEvent() {
        return onProgressUpdatedEvent;
    }

    public MutableLiveData<File> getOnSuccessEvent() {
        return onSuccessEvent;
    }

    public LiveEvent<ErrorModel> getErrorEvent() {
        return errorEvent;
    }
}
