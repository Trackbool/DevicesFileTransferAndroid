package com.afa.devicesfiletransfer.usecases;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.repository.TransfersRepository;

public class SaveTransferUseCase {
    private final TransfersRepository repository;

    public SaveTransferUseCase(TransfersRepository repository) {
        this.repository = repository;
    }

    public void execute(Transfer transfer, final Callback callback) {
        repository.saveTransfer(transfer, new TransfersRepository.AddTransferCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback {
        void onSuccess();

        void onError(Exception e);
    }
}
