package com.afa.devicesfiletransfer.view.ui.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Pair;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerReceiverImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.ui.BaseFragment;
import com.afa.devicesfiletransfer.view.ui.TransfersAdapter;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModelFactory;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TransfersFragment extends BaseFragment {
    private ReceiveTransferViewModel receiveTransferViewModel;
    private TransfersAdapter transfersAdapter;
    private RecyclerView transfersRecyclerView;

    public static TransfersFragment newInstance() {
        return new TransfersFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transfers, container, false);

        transfersRecyclerView = root.findViewById(R.id.transfersRecyclerView);
        initializeTransferReceiverViewModel();
        initializeRecyclerView();
        receiveTransferViewModel.onStart();

        return root;
    }

    private void initializeTransferReceiverViewModel() {
        FilesReceiverListenerServiceExecutor receiverServiceExecutor =
                new FilesReceiverListenerServiceExecutorImpl(requireActivity().getApplicationContext());
        FilesReceiverListenerReceiver filesReceiverListenerReceiver =
                new FilesReceiverListenerReceiverImpl(requireActivity().getApplicationContext());
        receiveTransferViewModel = new ViewModelProvider(this,
                new ReceiveTransferViewModelFactory(receiverServiceExecutor, filesReceiverListenerReceiver))
                .get(ReceiveTransferViewModel.class);
        receiveTransferViewModel.getOnSuccessEvent().observe(this, new Observer<Pair<Transfer, File>>() {
            @Override
            public void onChanged(Pair<Transfer, File> transferFilePair) {
                File file = transferFilePair.getRight();

                showAlert("File received", "The file " +
                        file.getName() + " has been received");
            }
        });
        receiveTransferViewModel.getErrorEvent().observe(this, new Observer<Pair<Transfer, ErrorModel>>() {
            @Override
            public void onChanged(Pair<Transfer, ErrorModel> transferErrorModelPair) {
                ErrorModel errorModel = transferErrorModelPair.getRight();

                showError(errorModel.getTitle(), errorModel.getMessage());
            }
        });
        receiveTransferViewModel.getTransferLiveData().observe(this, new Observer<List<Transfer>>() {
            @Override
            public void onChanged(List<Transfer> transfers) {
                transfersAdapter.setTransfers(transfers);
            }
        });
        receiveTransferViewModel.getOnProgressUpdatedEvent().observe(this, new Observer<Transfer>() {
            @Override
            public void onChanged(Transfer transfer) {
                transfersAdapter.refreshData();
            }
        });
    }

    private void initializeRecyclerView() {
        transfersRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        transfersRecyclerView.setLayoutManager(linearLayoutManager);
        transfersAdapter = new TransfersAdapter();
        transfersRecyclerView.setAdapter(transfersAdapter);
    }

    @Override
    public void onDestroyView() {
        receiveTransferViewModel.onDestroy();
        super.onDestroyView();
    }
}
