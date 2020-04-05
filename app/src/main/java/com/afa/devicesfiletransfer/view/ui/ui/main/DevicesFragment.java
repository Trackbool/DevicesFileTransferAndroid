package com.afa.devicesfiletransfer.view.ui.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryReceiver;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DevicesDiscoveryExecutorImpl;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DevicesDiscoveryReceiverImpl;
import com.afa.devicesfiletransfer.view.ui.BaseFragment;
import com.afa.devicesfiletransfer.view.ui.DevicesAdapter;
import com.afa.devicesfiletransfer.view.ui.SendFileActivity;
import com.afa.devicesfiletransfer.view.viewmodels.discovery.DiscoveryViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.discovery.DiscoveryViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DevicesFragment extends BaseFragment {
    private DiscoveryViewModel discoveryViewModel;
    private DevicesAdapter devicesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView devicesRecyclerView;
    private FloatingActionButton sendFilesButton;

    public static DevicesFragment newInstance() {
        return new DevicesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_devices, container, false);

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        devicesRecyclerView = root.findViewById(R.id.devicesRecyclerView);
        sendFilesButton = root.findViewById(R.id.sendFilesButton);
        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Device> devices = devicesAdapter.getSelectedDevices();
                openSendFileActivity(devices);
            }
        });
        initializeViews();
        initializeDiscoveryViewModel();

        return root;
    }

    private void initializeDiscoveryViewModel() {
        DevicesDiscoveryExecutor devicesDiscoveryExecutor = new DevicesDiscoveryExecutorImpl(
                requireActivity().getApplicationContext());
        DevicesDiscoveryReceiver devicesDiscoveryReceiver = new DevicesDiscoveryReceiverImpl(
                requireActivity().getApplicationContext());
        discoveryViewModel = new ViewModelProvider(requireActivity(),
                new DiscoveryViewModelFactory(devicesDiscoveryExecutor, devicesDiscoveryReceiver))
                .get(DiscoveryViewModel.class);

        discoveryViewModel.getDevicesLiveData().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                devicesAdapter.setDevices(devices);
            }
        });
        discoveryViewModel.getErrorEvent().observe(this, new Observer<ErrorModel>() {
            @Override
            public void onChanged(ErrorModel error) {
                showError(error.getTitle(), error.getMessage());
            }
        });
    }

    private void initializeViews() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                discoverDevices();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1200);
            }
        });
        initializeRecyclerView();
    }

    private void discoverDevices() {
        sendFilesButton.setVisibility(View.INVISIBLE);
        devicesAdapter.unselectDevices();
        discoveryViewModel.discoverDevices();
    }

    private void initializeRecyclerView() {
        devicesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        devicesRecyclerView.setLayoutManager(linearLayoutManager);
        devicesAdapter = new DevicesAdapter(new DevicesAdapter.Callback() {
            @Override
            public void onClick(Device device) {
                List<Device> devices = new ArrayList<>();
                devices.add(device);
                openSendFileActivity(devices);
            }

            @Override
            public void onItemSelected(Device device) {
                if (devicesAdapter.getSelectedDevices().size() >= 2) {
                    sendFilesButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemDeselected(Device device) {
                if (devicesAdapter.getSelectedDevices().size() < 2) {
                    sendFilesButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        devicesRecyclerView.setAdapter(devicesAdapter);
    }

    private void openSendFileActivity(List<Device> devices) {
        Intent intent = new Intent(requireActivity(), SendFileActivity.class);
        intent.putParcelableArrayListExtra("devicesList", new ArrayList<>(devices));
        startActivity(intent);
    }
}