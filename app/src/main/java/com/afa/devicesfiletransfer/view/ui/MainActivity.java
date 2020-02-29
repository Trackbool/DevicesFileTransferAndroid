package com.afa.devicesfiletransfer.view.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.view.discovery.DiscoveryContract;
import com.afa.devicesfiletransfer.view.discovery.DiscoveryPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements DiscoveryContract.View {

    private DiscoveryContract.Presenter presenter;
    private DevicesAdapter devicesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MenuItem sendMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        presenter = new DiscoveryPresenter(this);
        presenter.onViewLoaded();
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
                presenter.onDiscoverDevicesEvent();
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

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}
