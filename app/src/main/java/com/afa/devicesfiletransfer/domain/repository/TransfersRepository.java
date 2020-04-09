package com.afa.devicesfiletransfer.domain.repository;

import com.afa.devicesfiletransfer.domain.model.Transfer;

import java.util.List;

public interface TransfersRepository {
    void getTransfers(GetTransfersCallback callback);

    void getLastTransfers(int max, GetTransfersCallback callback);

    void saveTransfer(Transfer transfer, AddTransferCallback callback);

    interface GetTransfersCallback {
        void onSuccess(List<Transfer> transfers);

        void onError(Exception e);
    }

    interface AddTransferCallback {
        void onSuccess();

        void onError(Exception e);
    }
}
