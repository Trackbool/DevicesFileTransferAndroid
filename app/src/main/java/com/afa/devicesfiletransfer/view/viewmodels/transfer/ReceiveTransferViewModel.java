package com.afa.devicesfiletransfer.view.viewmodels.transfer;

import com.afa.devicesfiletransfer.model.Pair;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
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
    private final MutableLiveData<Transfer> onProgressUpdatedEvent;
    private final MutableLiveData<Pair<Transfer, File>> onSuccessEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> errorEvent;
    private FilesReceiverListenerServiceExecutor receiverServiceExecutor;
    private FilesReceiverListenerReceiver receiverListenerReceiver;

    public ReceiveTransferViewModel(FilesReceiverListenerServiceExecutor receiverServiceExecutor,
                                    FilesReceiverListenerReceiver receiverListenerReceiver) {
        transfers = new ArrayList<>();
        transferLiveData = new MutableLiveData<>();
        onProgressUpdatedEvent = new MutableLiveData<>();
        onSuccessEvent = new MutableLiveData<>();
        errorEvent = new LiveEvent<>();
        this.receiverServiceExecutor = receiverServiceExecutor;
        this.receiverListenerReceiver = receiverListenerReceiver;
        this.receiverServiceExecutor.start();
        this.receiverListenerReceiver.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transferLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                errorEvent.postValue(
                        new Pair<>(transfer, new ErrorModel("Receiving error", e.getMessage())));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onProgressUpdatedEvent.postValue(null);
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                onSuccessEvent.postValue(new Pair<>(transfer, file));
            }
        });
    }

    public void onStart() {
        this.receiverListenerReceiver.receive();
    }

    public MutableLiveData<List<Transfer>> getTransferLiveData() {
        return transferLiveData;
    }

    public MutableLiveData<Transfer> getOnProgressUpdatedEvent() {
        return onProgressUpdatedEvent;
    }

    public MutableLiveData<Pair<Transfer, File>> getOnSuccessEvent() {
        return onSuccessEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getErrorEvent() {
        return errorEvent;
    }


    public void onDestroy() {
        this.receiverListenerReceiver.stop();
    }
}
