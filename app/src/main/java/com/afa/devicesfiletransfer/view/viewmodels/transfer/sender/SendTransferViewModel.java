package com.afa.devicesfiletransfer.view.viewmodels.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Pair;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.livedata.LiveEvent;
import com.afa.devicesfiletransfer.view.framework.model.AlertModel;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SendTransferViewModel extends ViewModel {
    private final List<Transfer> transfers;
    private final MutableLiveData<List<Transfer>> transfersLiveData;
    private final MutableLiveData<Transfer> onTransferProgressUpdatedEvent;
    private final MutableLiveData<Pair<Transfer, TransferFile>> onTransferSucceededEvent;
    private final MutableLiveData<TransferFile> attachedFile;
    private final LiveEvent<AlertModel> alertEvent;
    private final LiveEvent<ErrorModel> errorEvent;
    private final FileSenderServiceExecutor fileSenderExecutor;
    private final FileSenderReceiver fileSenderReceiver;

    public SendTransferViewModel(FileSenderServiceExecutor fileSenderExecutor,
                                 FileSenderReceiver fileSenderReceiver) {
        transfers = new ArrayList<>();
        transfersLiveData = new MutableLiveData<>();
        onTransferProgressUpdatedEvent = new MutableLiveData<>();
        onTransferSucceededEvent = new MutableLiveData<>();
        attachedFile = new MutableLiveData<>();
        alertEvent = new LiveEvent<>();
        errorEvent = new LiveEvent<>();

        this.fileSenderExecutor = fileSenderExecutor;
        this.fileSenderReceiver = fileSenderReceiver;
        this.fileSenderReceiver.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                //TODO: Store in database
                transfers.add(transfer);
                transfersLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                triggerErrorEvent("Sending error", e.getMessage());
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                onTransferSucceededEvent.postValue(new Pair<>(transfer, file));
            }
        });
    }

    public void onStart() {
        fileSenderReceiver.receive();
    }

    public MutableLiveData<List<Transfer>> getTransfersLiveData() {
        return transfersLiveData;
    }

    public MutableLiveData<Transfer> getOnTransferProgressUpdatedEvent() {
        return onTransferProgressUpdatedEvent;
    }

    public MutableLiveData<Pair<Transfer, TransferFile>> getOnTransferSucceededEvent() {
        return onTransferSucceededEvent;
    }

    public MutableLiveData<TransferFile> getAttachedFile() {
        return attachedFile;
    }

    public LiveEvent<AlertModel> getAlertEvent() {
        return alertEvent;
    }

    public LiveEvent<ErrorModel> getErrorEvent() {
        return errorEvent;
    }

    public void attachFile(TransferFile file) {
        this.attachedFile.postValue(file);
    }

    public void sendFile(List<Device> devices) {
        if (attachedFile == null) {
            triggerErrorEvent("No file attached", "You must attach a file");
            return;
        }

        if (devices == null || devices.size() == 0) {
            showAlert("No device selected",
                    "You must select one or more devices to send the file");
            return;
        }

        for (Device device : devices) {
            sendFile(device);
        }
    }

    private void sendFile(Device device) {
        fileSenderExecutor.send(device, attachedFile.getValue());
    }

    private void triggerErrorEvent(String title, String message) {
        errorEvent.postValue(new ErrorModel(title, message));
    }

    private void showAlert(String title, String message) {
        alertEvent.postValue(new AlertModel(title, message));
    }

    public void onDestroy() {
        fileSenderReceiver.stop();
    }
}
