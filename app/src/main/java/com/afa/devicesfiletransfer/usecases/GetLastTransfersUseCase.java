package com.afa.devicesfiletransfer.usecases;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.repository.TransfersRepository;

import java.util.List;

public class GetLastTransfersUseCase {
    private final TransfersRepository repository;

    public GetLastTransfersUseCase(TransfersRepository repository) {
        this.repository = repository;
    }

    public void execute(int max, final Callback callback) {
        repository.getLastTransfers(max, new TransfersRepository.GetTransfersCallback() {
            @Override
            public void onSuccess(List<Transfer> transfers) {
                callback.onSuccess(transfers);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback {
        void onSuccess(List<Transfer> transfers);

        void onError(Exception e);
    }
}
