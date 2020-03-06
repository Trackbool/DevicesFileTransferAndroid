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
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.services.transfer.receiver.FilesReceiverListenerServiceExecutor;
import com.afa.devicesfiletransfer.util.SystemUtils;
import com.afa.devicesfiletransfer.view.presenters.discovery.DiscoveryContract;
import com.afa.devicesfiletransfer.view.presenters.discovery.DiscoveryPresenter;
import com.afa.devicesfiletransfer.view.framework.services.receiver.FilesReceiverListenerServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.presenters.transfer.receiver.ReceiveTransferContract;
import com.afa.devicesfiletransfer.view.presenters.transfer.receiver.ReceiveTransferPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements DiscoveryContract.View, ReceiveTransferContract.View {

    private DiscoveryContract.Presenter discoveryPresenter;
    private ReceiveTransferPresenter receiveTransferPresenter;
    private DevicesAdapter devicesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MenuItem sendMenuButton;

    private static final int REQUEST_READ_WRITE_PERMISSION = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        discoveryPresenter = new DiscoveryPresenter(this);
        FilesReceiverListenerServiceExecutor receiverServiceExecutor =
                new FilesReceiverListenerServiceExecutorImpl(getApplicationContext());
        receiveTransferPresenter = new ReceiveTransferPresenter(this, receiverServiceExecutor);
        requestStoragePermissions();
        discoveryPresenter.onViewLoaded();
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
        } else {
            receiveTransferPresenter.onViewLoaded();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
        } else {
            receiveTransferPresenter.onViewLoaded();
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
                discoveryPresenter.onDiscoverDevicesEvent();
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

    private void openSendFileActivity(List<Device> devices) {
        Intent intent = new Intent(this, SendFileActivity.class);
        intent.putParcelableArrayListExtra("devicesList", new ArrayList<>(devices));
        startActivity(intent);
    }

    @Override
    public void addDevice(final Device device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devicesAdapter.addDevice(device);
            }
        });
    }

    @Override
    public void showError(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        title + ". " + message,
                        Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(Color.RED);
                snackbar.show();
            }
        });
    }

    @Override
    public List<Device> getDevicesList() {
        return devicesAdapter.getDevices();
    }

    @Override
    public void clearDevicesList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devicesAdapter.clearDevices();
            }
        });
    }

    @Override
    public void close() {
        finish();
    }

    //Receiver listener
    @Override
    public void showAlert(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
                        title + ". " + message,
                        Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    public void addReceptionTransfer(Transfer transfer) {
        Log.d("ADRI-DEBUG", "VIEW TRANSFER: " + transfer.getDeviceName());
    }

    @Override
    public void refreshReceptionsData() {

    }

    @Override
    public File getDownloadsDirectory() {
        return SystemUtils.getDownloadsDirectory();
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

    @Override
    protected void onDestroy() {
        discoveryPresenter.onDestroy();
        receiveTransferPresenter.onDestroy();
        super.onDestroy();
    }
}
