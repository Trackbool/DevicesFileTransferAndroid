package com.afa.devicesfiletransfer.view.viewmodels.transfer.receiver;

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
    private final MutableLiveData<Transfer> onTransferProgressUpdatedEvent;
    private final MutableLiveData<Pair<Transfer, File>> onTransferSucceededEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> errorEvent;
    private FilesReceiverListenerServiceExecutor receiverServiceExecutor;
    private FileReceiverProtocol.Callback fileReceiverCallback;
    private FilesReceiverListenerReceiver receiverListenerReceiver;

    public ReceiveTransferViewModel(FilesReceiverListenerServiceExecutor receiverServiceExecutor,
                                    FilesReceiverListenerReceiver receiverListenerReceiver) {
        transfers = new ArrayList<>();
        transferLiveData = new MutableLiveData<>();
        onTransferProgressUpdatedEvent = new MutableLiveData<>();
        onTransferSucceededEvent = new MutableLiveData<>();
        errorEvent = new LiveEvent<>();
        this.receiverServiceExecutor = receiverServiceExecutor;
        this.receiverListenerReceiver = receiverListenerReceiver;
        this.receiverServiceExecutor.start();
        fileReceiverCallback = new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transferLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                errorEvent.postValue(new Pair<>(transfer,
                        new ErrorModel("Receiving error", e.getMessage())));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                onTransferSucceededEvent.postValue(new Pair<>(transfer, file));
            }
        };
        this.receiverListenerReceiver.setCallback(fileReceiverCallback);
    }

    public void onStart() {
        this.receiverListenerReceiver.receive();
    }

    public MutableLiveData<List<Transfer>> getTransferLiveData() {
        return transferLiveData;
    }

    public MutableLiveData<Transfer> getOnTransferProgressUpdatedEvent() {
        return onTransferProgressUpdatedEvent;
    }

    public MutableLiveData<Pair<Transfer, File>> getOnTransferSucceededEvent() {
        return onTransferSucceededEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getErrorEvent() {
        return errorEvent;
    }

    public void onDestroy() {
        if (receiverListenerReceiver != null)
            this.receiverListenerReceiver.stop();
    }

    @Override
    protected void onCleared() {
        receiverListenerReceiver.setCallback(null);
        super.onCleared();
    }
}
