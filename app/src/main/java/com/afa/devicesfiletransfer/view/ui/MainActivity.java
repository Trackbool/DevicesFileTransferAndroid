package com.afa.devicesfiletransfer.view.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryExecutor;
import com.afa.devicesfiletransfer.services.discovery.DevicesDiscoveryReceiver;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.model.ErrorModel;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DevicesDiscoveryExecutorImpl;
import com.afa.devicesfiletransfer.view.framework.services.discovery.DevicesDiscoveryReceiverImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.receiver.FilesReceiverListenerServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.viewmodels.discovery.DiscoveryViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.discovery.DiscoveryViewModelFactory;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.ReceiveTransferViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {
    private DiscoveryViewModel discoveryViewModel;
    private ReceiveTransferViewModel receiveTransferViewModel;
    private DevicesAdapter devicesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MenuItem sendMenuButton;

    private static final int REQUEST_READ_WRITE_PERMISSION = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeDiscoveryViewModel();
        requestStoragePermissions();
        initializeTransferReceiverViewModel();
    }

    private void requestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Storage permissions")
                        .setMessage("The access storage permissions are required for the app")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_READ_WRITE_PERMISSION);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_READ_WRITE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.devices_menu, menu);
        sendMenuButton = menu.findItem(R.id.toTransferScreenButton);
        sendMenuButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toTransferScreenButton) {
            List<Device> devices = devicesAdapter.getSelectedDevices();
            openSendFileActivity(devices);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                discoveryViewModel.discoverDevices();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        initializeRecyclerView();
    }

    private void initializeRecyclerView() {
        RecyclerView devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        devicesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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
                    sendMenuButton.setVisible(true);
                }
            }

            @Override
            public void onItemDeselected(Device device) {
                if (devicesAdapter.getSelectedDevices().size() < 2) {
                    sendMenuButton.setVisible(false);
                }
            }
        });
        devicesRecyclerView.setAdapter(devicesAdapter);
    }

    private void initializeDiscoveryViewModel() {
        DevicesDiscoveryExecutor devicesDiscoveryExecutor = new DevicesDiscoveryExecutorImpl(getApplicationContext());
        DevicesDiscoveryReceiver devicesDiscoveryReceiver = new DevicesDiscoveryReceiverImpl(getApplicationContext());
        discoveryViewModel = new ViewModelProvider(this,
                new DiscoveryViewModelFactory(devicesDiscoveryExecutor, devicesDiscoveryReceiver))
                .get(DiscoveryViewModel.class);

        discoveryViewModel.getDevicesLiveData().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                Log.d("ADRI-DEBUG", "ACTIVITY: Hemos llegao");
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

    private void initializeTransferReceiverViewModel() {
        FilesReceiverListenerServiceExecutor receiverServiceExecutor =
                new FilesReceiverListenerServiceExecutorImpl(getApplicationContext());
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

    private void openSendFileActivity(List<Device> devices) {
        Intent intent = new Intent(this, SendFileActivity.class);
        intent.putParcelableArrayListExtra("devicesList", new ArrayList<>(devices));
        startActivity(intent);
    }

    public void showError(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }

    public void showAlert(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit the app?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
