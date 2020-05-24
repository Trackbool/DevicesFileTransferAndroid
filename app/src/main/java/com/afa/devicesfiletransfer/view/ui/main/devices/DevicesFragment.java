package com.afa.devicesfiletransfer.view.ui.main.devices;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceInteractor;
import com.afa.devicesfiletransfer.services.discovery.DiscoveryServiceLauncher;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DiscoveryServiceInteractorImpl;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DiscoveryServiceLauncherImpl;
import com.afa.devicesfiletransfer.view.model.ErrorModel;
import com.afa.devicesfiletransfer.view.ui.BaseFragment;
import com.afa.devicesfiletransfer.view.ui.SendFileActivity;
import com.afa.devicesfiletransfer.view.ui.main.Backable;
import com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel.DevicesViewModel;
import com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel.DevicesViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DevicesFragment extends BaseFragment implements Backable {
    private DevicesViewModel devicesViewModel;
    private DevicesAdapter devicesAdapter;
    private ImageView questionIconImageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView devicesRecyclerView;
    private FloatingActionButton sendFilesButton;
    private PopupWindow headerTipPopup;

    public static DevicesFragment newInstance() {
        return new DevicesFragment();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_devices, container, false);

        questionIconImageView = root.findViewById(R.id.questionIconImageView);
        headerTipPopup = createTipPopup();
        questionIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headerTipPopup.showAsDropDown(questionIconImageView);
            }
        });
        ImageView addDirectConnectionImageView = root.findViewById(R.id.addDirectConnectionImageView);
        addDirectConnectionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOverlayForm();
            }
        });
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
        initializeDevicesViewModel();
        devicesViewModel.onStart();

        return root;
    }

    private void showOverlayForm() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        DialogFragment dialog = new AddDeviceDialogFragment();
        dialog.show(fragmentManager, "dialog");
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

    private void initializeDevicesViewModel() {
        DiscoveryServiceLauncher discoveryServiceLauncher = new DiscoveryServiceLauncherImpl(
                requireActivity().getApplicationContext());
        DiscoveryServiceInteractor discoveryServiceInteractor = new DiscoveryServiceInteractorImpl(
                requireActivity().getApplicationContext());
        devicesViewModel = new ViewModelProvider(this,
                new DevicesViewModelFactory(discoveryServiceLauncher, discoveryServiceInteractor))
                .get(DevicesViewModel.class);

        devicesViewModel.getDevicesLiveData().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                devicesAdapter.setDevices(devices);
            }
        });
        devicesViewModel.getErrorEvent().observe(this, new Observer<ErrorModel>() {
            @Override
            public void onChanged(ErrorModel error) {
                showError(error.getTitle(), error.getMessage());
            }
        });
    }

    private PopupWindow createTipPopup() {
        final PopupWindow tip = new PopupWindow(requireContext());

        TextView tipText = new TextView(requireContext());
        tipText.setText(getString(R.string.devices_header_question_tip_text));
        tipText.setTextColor(Color.WHITE);

        FrameLayout textContainer = new FrameLayout(requireContext());
        textContainer.setBackgroundColor(Color.BLACK);
        textContainer.getBackground().setAlpha(200);
        textContainer.setPadding(20, 20, 20, 20);
        textContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tip.dismiss();
            }
        });
        textContainer.addView(tipText);

        tip.setOutsideTouchable(true);
        tip.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tip.setContentView(textContainer);
        tip.setHeight(200);
        tip.setWidth(700);

        return tip;
    }

    private void discoverDevices() {
        deselectAdapterDevices();
        devicesViewModel.discoverDevices();
    }

    private void deselectAdapterDevices() {
        sendFilesButton.setVisibility(View.INVISIBLE);
        devicesAdapter.deselectDevices();
    }

    private void openSendFileActivity(List<Device> devices) {
        Intent intent = new Intent(requireActivity(), SendFileActivity.class);
        intent.putParcelableArrayListExtra("devicesList", new ArrayList<>(devices));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        devicesViewModel.onDestroy();
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressed() {
        headerTipPopup.dismiss();
        if (!devicesAdapter.getSelectedDevices().isEmpty()) {
            deselectAdapterDevices();
            return false;
        }
        return true;
    }
}