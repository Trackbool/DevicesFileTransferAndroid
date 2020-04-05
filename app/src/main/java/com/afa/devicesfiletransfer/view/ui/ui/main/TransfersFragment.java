package com.afa.devicesfiletransfer.view.ui.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.ui.BaseFragment;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModelFactory;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class TransfersFragment extends BaseFragment {

    private ReceiveTransferViewModel receiveTransferViewModel;

    public static TransfersFragment newInstance() {
        return new TransfersFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transfers, container, false);
        initializeTransferReceiverViewModel();

        return root;
    }

    private void initializeTransferReceiverViewModel() {
        FilesReceiverListenerServiceExecutor receiverServiceExecutor =
                new FilesReceiverListenerServiceExecutorImpl(requireActivity().getApplicationContext());
        receiveTransferViewModel = new ViewModelProvider(this,
                new ReceiveTransferViewModelFactory(receiverServiceExecutor))
                .get(ReceiveTransferViewModel.class);
        receiveTransferViewModel.getOnSuccessEvent().observe(this, new Observer<File>() {
            @Override
            public void onChanged(File file) {
                showAlert("File received", "The file " +
                        file.getName() + " has been received");
            }
        });
        receiveTransferViewModel.getErrorEvent().observe(this, new Observer<ErrorModel>() {
            @Override
            public void onChanged(ErrorModel errorModel) {
                showError(errorModel.getTitle(), errorModel.getMessage());
            }
        });
    }
}
