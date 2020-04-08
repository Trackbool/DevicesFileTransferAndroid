package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import com.afa.devicesfiletransfer.model.Pair;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.util.TransferDateComparator;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.model.ErrorModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransfersViewModel extends ViewModel {
    private final List<Transfer> transfers;
    private final MutableLiveData<List<Transfer>> transfersLiveData;
    private final MutableLiveData<Transfer> onTransferProgressUpdatedEvent;
    private final MutableLiveData<Pair<Transfer, File>> onSendTransferSucceededEvent;
    private final MutableLiveData<Pair<Transfer, File>> onReceiveTransferSucceededEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> onSendTransferErrorEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> onReceiveTransferErrorEvent;
    private final FilesReceiverListenerServiceExecutor receiverServiceExecutor;
    private final FilesReceiverListenerReceiver receiverListenerReceiver;
    private final FileSenderReceiver fileSenderReceiver;

    public TransfersViewModel(FilesReceiverListenerServiceExecutor receiverServiceExecutor,
                              final FilesReceiverListenerReceiver receiverListenerReceiver,
                              final FileSenderReceiver fileSenderReceiver) {
        transfers = new ArrayList<Transfer>() {
            @Override
            public boolean add(Transfer transfer) {
                super.add(transfer);
                Collections.sort(transfers, new TransferDateComparator(true));
                return true;
            }
        };
        transfersLiveData = new MutableLiveData<>();
        onTransferProgressUpdatedEvent = new MutableLiveData<>();
        onSendTransferSucceededEvent = new MutableLiveData<>();
        onReceiveTransferSucceededEvent = new MutableLiveData<>();
        onSendTransferErrorEvent = new LiveEvent<>();
        onReceiveTransferErrorEvent = new LiveEvent<>();
        this.receiverServiceExecutor = receiverServiceExecutor;
        this.receiverListenerReceiver = receiverListenerReceiver;
        this.fileSenderReceiver = fileSenderReceiver;

        this.receiverListenerReceiver.setServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onConnect() {
                List<Transfer> inProgressTransfers =
                        receiverListenerReceiver.getInProgressTransfers();
                addInProgressTransfersIfThereAreNotAdded(inProgressTransfers);
            }

            @Override
            public void onDisconnect() {

            }
        });
        final FileReceiverProtocol.Callback fileReceiverCallback = createFileReceiverCallback();
        this.receiverListenerReceiver.setCallback(fileReceiverCallback);

        this.fileSenderReceiver.setServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onConnect() {
                List<Transfer> inProgressTransfers =
                        fileSenderReceiver.getInProgressTransfers();
                addInProgressTransfersIfThereAreNotAdded(inProgressTransfers);
            }

            @Override
            public void onDisconnect() {

            }
        });
        final FileSenderProtocol.Callback fileSenderCallback = createFileSenderCallback();
        this.fileSenderReceiver.setCallback(fileSenderCallback);

        this.receiverServiceExecutor.start();
    }

    private FileReceiverProtocol.Callback createFileReceiverCallback() {
        return new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transfersLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                triggerReceiveTransferErrorEvent(transfer,
                        new ErrorModel("Receiving error", e.getMessage()));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                onReceiveTransferSucceededEvent.postValue(new Pair<>(transfer, file));
            }
        };
    }

    private FileSenderProtocol.Callback createFileSenderCallback() {
        return new FileSenderProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transfersLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                triggerSendTransferErrorEvent(transfer,
                        new ErrorModel("Sending error", e.getMessage()));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                File transferFile = new File(file.getPath());
                onSendTransferSucceededEvent.postValue(new Pair<>(transfer, transferFile));
            }
        };
    }

    public MutableLiveData<List<Transfer>> getTransfersLiveData() {
        return transfersLiveData;
    }

    public MutableLiveData<Transfer> getOnTransferProgressUpdatedEvent() {
        return onTransferProgressUpdatedEvent;
    }

    public MutableLiveData<Pair<Transfer, File>> getOnSendTransferSucceededEvent() {
        return onSendTransferSucceededEvent;
    }

    public MutableLiveData<Pair<Transfer, File>> getOnReceiveTransferSucceededEvent() {
        return onReceiveTransferSucceededEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getOnSendTransferErrorEvent() {
        return onSendTransferErrorEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getOnReceiveTransferErrorEvent() {
        return onReceiveTransferErrorEvent;
    }

    public void onStart() {
        this.receiverListenerReceiver.receive();
        this.fileSenderReceiver.receive();
    }

    private void addInProgressTransfersIfThereAreNotAdded(List<Transfer> inProgressTransfers) {
        for (Transfer t : inProgressTransfers) {
            if (!transfers.contains(t)) {
                transfers.add(t);
            }
        }
        transfersLiveData.postValue(transfers);
    }

    private void triggerSendTransferErrorEvent(Transfer transfer, ErrorModel error) {
        onSendTransferErrorEvent.postValue(new Pair<>(transfer, error));
    }

    private void triggerReceiveTransferErrorEvent(Transfer transfer, ErrorModel error) {
        onReceiveTransferErrorEvent.postValue(new Pair<>(transfer, error));
    }

    public void onDestroy() {
        if (receiverListenerReceiver != null)
            receiverListenerReceiver.stop();

        if (fileSenderReceiver != null)
            fileSenderReceiver.stop();
    }

    @Override
    protected void onCleared() {
        receiverListenerReceiver.setServiceConnectionCallback(null);
        receiverListenerReceiver.setCallback(null);
        fileSenderReceiver.setCallback(null);
        super.onCleared();
    }
}
