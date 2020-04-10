package com.afa.devicesfiletransfer.view.ui.main.transfers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Pair;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.view.model.ErrorModel;
import com.afa.devicesfiletransfer.view.ui.BaseFragment;
import com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel.TransfersViewModel;
import com.afa.devicesfiletransfer.view.ui.main.transfers.viewmodel.TransfersViewModelFactory;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TransfersFragment extends BaseFragment {
    private TransfersViewModel transfersViewModel;
    private TransfersAdapter transfersAdapter;
    private RecyclerView transfersRecyclerView;
    private ProgressBar progressBar;

    public static TransfersFragment newInstance() {
        return new TransfersFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transfers, container, false);

        transfersRecyclerView = root.findViewById(R.id.transfersRecyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        initializeTransferReceiverViewModel();
        initializeRecyclerView();

        return root;
    }

    private void initializeTransferReceiverViewModel() {
        transfersViewModel = new ViewModelProvider(this,
                new TransfersViewModelFactory(requireActivity().getApplicationContext()))
                .get(TransfersViewModel.class);

        transfersViewModel.getLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        transfersViewModel.getTransfersLiveData().observe(this, new Observer<List<Transfer>>() {
            @Override
            public void onChanged(List<Transfer> transfers) {
                transfersAdapter.setTransfers(transfers);
            }
        });
        transfersViewModel.getOnReceiveTransferSucceededEvent().observe(this, new Observer<Pair<Transfer, File>>() {
            @Override
            public void onChanged(Pair<Transfer, File> transferFilePair) {
                File file = transferFilePair.getRight();
                showAlert("File received", "The file " +
                        file.getName() + " has been received");
            }
        });
        transfersViewModel.getOnSendTransferSucceededEvent().observe(this, new Observer<Pair<Transfer, File>>() {
            @Override
            public void onChanged(Pair<Transfer, File> transferFilePair) {
                File file = transferFilePair.getRight();
                showAlert("File sent", "The file " +
                        file.getName() + " has sent");
            }
        });
        transfersViewModel.getOnReceiveTransferErrorEvent().observe(this, new Observer<Pair<Transfer, ErrorModel>>() {
            @Override
            public void onChanged(Pair<Transfer, ErrorModel> transferErrorModelPair) {
                ErrorModel error = transferErrorModelPair.getRight();
                showError("Receiving error", error.getMessage());
            }
        });
        transfersViewModel.getOnSendTransferErrorEvent().observe(this, new Observer<Pair<Transfer, ErrorModel>>() {
            @Override
            public void onChanged(Pair<Transfer, ErrorModel> transferErrorModelPair) {
                ErrorModel error = transferErrorModelPair.getRight();
                showError("Sending error", error.getMessage());
            }
        });
        transfersViewModel.getOnTransferProgressUpdatedEvent().observe(this, new Observer<Transfer>() {
            @Override
            public void onChanged(Transfer transfer) {
                transfersAdapter.refreshData();
            }
        });
        transfersViewModel.onStart();
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
        transfersViewModel.onDestroy();
        super.onDestroyView();
    }
}
