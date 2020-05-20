package com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel;

import com.afa.devicesfiletransfer.domain.model.Pair;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverProtocol;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverServiceInteractor;
import com.afa.devicesfiletransfer.services.transfer.receiver.FileReceiverServiceLauncher;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderProtocol;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceInteractor;
import com.afa.devicesfiletransfer.usecases.GetLastTransfersUseCase;
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
    private final MutableLiveData<Boolean> loading;
    private final List<Transfer> transfers;
    private final MutableLiveData<List<Transfer>> transfersLiveData;
    private final LiveEvent<Transfer> onTransferProgressUpdatedEvent;
    private final LiveEvent<Pair<Transfer, File>> onSendTransferSucceededEvent;
    private final LiveEvent<Pair<Transfer, File>> onReceiveTransferSucceededEvent;
    private final LiveEvent<ErrorModel> onErrorEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> onSendTransferErrorEvent;
    private final LiveEvent<Pair<Transfer, ErrorModel>> onReceiveTransferErrorEvent;

    private final GetLastTransfersUseCase getLastTransfersUseCase;
    private final FileReceiverServiceLauncher receiverServiceExecutor;
    private final FileReceiverServiceInteractor receiverListenerInteractor;
    private final FileSenderServiceInteractor fileSenderServiceInteractor;

    public TransfersViewModel(final GetLastTransfersUseCase getLastTransfersUseCase,
                              final FileReceiverServiceLauncher receiverServiceExecutor,
                              final FileReceiverServiceInteractor receiverListenerInteractor,
                              final FileSenderServiceInteractor fileSenderServiceInteractor) {
        loading = new MutableLiveData<>();
        transfers = new ArrayList<Transfer>() {
            @Override
            public boolean add(Transfer transfer) {
                super.add(transfer);
                Collections.sort(transfers, new TransferDateComparator(true));
                return true;
            }
        };
        transfersLiveData = new MutableLiveData<>();
        onTransferProgressUpdatedEvent = new LiveEvent<>();
        onSendTransferSucceededEvent = new LiveEvent<>();
        onReceiveTransferSucceededEvent = new LiveEvent<>();
        onErrorEvent = new LiveEvent<>();
        onSendTransferErrorEvent = new LiveEvent<>();
        onReceiveTransferErrorEvent = new LiveEvent<>();
        this.getLastTransfersUseCase = getLastTransfersUseCase;
        this.receiverServiceExecutor = receiverServiceExecutor;
        this.receiverListenerInteractor = receiverListenerInteractor;
        this.fileSenderServiceInteractor = fileSenderServiceInteractor;

        this.receiverListenerInteractor.setServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onConnect() {
                List<Transfer> inProgressTransfers =
                        receiverListenerInteractor.getInProgressTransfers();
                addNewTransfers(inProgressTransfers);
            }

            @Override
            public void onDisconnect() {
            }
        });
        final FileReceiverProtocol.Callback fileReceiverCallback = createFileReceiverCallback();
        this.receiverListenerInteractor.setCallback(fileReceiverCallback);

        this.fileSenderServiceInteractor.setServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onConnect() {
                List<Transfer> inProgressTransfers =
                        fileSenderServiceInteractor.getInProgressTransfers();
                addNewTransfers(inProgressTransfers);
            }

            @Override
            public void onDisconnect() {
            }
        });
        final FileSenderProtocol.Callback fileSenderCallback = createFileSenderCallback();
        this.fileSenderServiceInteractor.setCallback(fileSenderCallback);

        this.receiverServiceExecutor.start();
        callGetLastTransfersUseCase();
    }

    private FileReceiverProtocol.Callback createFileReceiverCallback() {
        return new FileReceiverProtocol.Callback() {
            @Override
            public void onInitializationFailure() {
                triggerErrorEvent("Receiving error", "Could not connect");
            }

            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transfersLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                transfersLiveData.postValue(transfers);
                triggerReceiveTransferErrorEvent(transfer,
                        new ErrorModel("Receiving error", e.getMessage()));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                transfersLiveData.postValue(transfers);
                onReceiveTransferSucceededEvent.postValue(new Pair<>(transfer, file));
            }
        };
    }

    private FileSenderProtocol.Callback createFileSenderCallback() {
        return new FileSenderProtocol.Callback() {

            @Override
            public void onInitializationFailure(FileSenderProtocol fileSenderProtocol) {
                String targetDeviceName = fileSenderProtocol.getTargetDevice().getName();
                triggerErrorEvent("Sending error",
                        "Could not connect with " + targetDeviceName);
            }

            @Override
            public void onTransferInitializationFailure(Transfer transfer, Exception e) {

            }

            @Override
            public void onStart(Transfer transfer) {
                transfers.add(transfer);
                transfersLiveData.postValue(transfers);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                transfersLiveData.postValue(transfers);
                triggerSendTransferErrorEvent(transfer,
                        new ErrorModel("Sending error", e.getMessage()));
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                onTransferProgressUpdatedEvent.postValue(transfer);
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                transfersLiveData.postValue(transfers);
                File transferFile = new File(file.getPath());
                onSendTransferSucceededEvent.postValue(new Pair<>(transfer, transferFile));
            }
        };
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
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

    public LiveEvent<ErrorModel> getOnErrorEvent() {
        return onErrorEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getOnSendTransferErrorEvent() {
        return onSendTransferErrorEvent;
    }

    public LiveEvent<Pair<Transfer, ErrorModel>> getOnReceiveTransferErrorEvent() {
        return onReceiveTransferErrorEvent;
    }

    public void onStart() {
        this.receiverListenerInteractor.receive();
        this.fileSenderServiceInteractor.receive();
    }

    private void callGetLastTransfersUseCase() {
        loading.postValue(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int maxResults = 20;
                getLastTransfersUseCase.execute(maxResults, new GetLastTransfersUseCase.Callback() {
                    @Override
                    public void onSuccess(List<Transfer> transfers) {
                        addNewTransfers(transfers);
                        loading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        triggerErrorEvent("Error retrieving stored transfers", e.getMessage());
                        loading.postValue(false);
                    }
                });
            }
        }).start();
    }

    private void addNewTransfers(List<Transfer> newTransfers) {
        for (Transfer t : newTransfers) {
            if (!transfers.contains(t)) {
                transfers.add(t);
            }
        }
        transfersLiveData.postValue(transfers);
    }

    private void triggerErrorEvent(String title, String message) {
        onErrorEvent.postValue(new ErrorModel(title, message));
    }

    private void triggerSendTransferErrorEvent(Transfer transfer, ErrorModel error) {
        onSendTransferErrorEvent.postValue(new Pair<>(transfer, error));
    }

    private void triggerReceiveTransferErrorEvent(Transfer transfer, ErrorModel error) {
        onReceiveTransferErrorEvent.postValue(new Pair<>(transfer, error));
    }

    public void onDestroy() {
        if (receiverListenerInteractor != null)
            receiverListenerInteractor.stop();

        if (fileSenderServiceInteractor != null)
            fileSenderServiceInteractor.stop();
    }

    @Override
    protected void onCleared() {
        receiverListenerInteractor.setServiceConnectionCallback(null);
        receiverListenerInteractor.setCallback(null);
        fileSenderServiceInteractor.setCallback(null);
        super.onCleared();
    }
}
