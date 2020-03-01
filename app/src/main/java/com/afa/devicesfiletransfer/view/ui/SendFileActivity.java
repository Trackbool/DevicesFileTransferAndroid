package com.afa.devicesfiletransfer.view.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.util.SystemUtils;
import com.afa.devicesfiletransfer.view.transfer.TransferContract;
import com.afa.devicesfiletransfer.view.transfer.TransferPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class SendFileActivity extends AppCompatActivity implements TransferContract.View {

    private TransferContract.Presenter presenter;
    private List<Device> devices;
    private Button attachFileButton;
    private TextView attachedFileNameTextView;
    private Button sendFileButton;

    private static final int BROWSE_FILE_RESULT_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        devices = Objects.requireNonNull(
                getIntent().getExtras()).getParcelableArrayList("devicesList");

        presenter = new TransferPresenter(this);
        attachFileButton = findViewById(R.id.attachFileButton);
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onBrowseFileButtonClicked();
            }
        });
        attachedFileNameTextView = findViewById(R.id.attachedFileNameTextView);
        sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSendFileButtonClicked();
            }
        });
        presenter.onViewLoaded();
    }

    @Override
    public void showError(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(SendFileActivity.this.findViewById(android.R.id.content),
                        title + ". " + message,
                        Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(Color.RED);
                snackbar.show();
                Log.d("MENSAJE ERROR", message);
            }
        });
    }

    @Override
    public void showAlert(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar.make(SendFileActivity.this.findViewById(android.R.id.content),
                        title + ". " + message,
                        Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    public void browseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, BROWSE_FILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BROWSE_FILE_RESULT_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                File file = new File(data.getData().getPath());
                presenter.onFileAttached(file);
            }
        }
    }

    @Override
    public void showFileAttachedName(String name) {
        attachedFileNameTextView.setText(name);
    }

    @Override
    public Device[] getSelectedDevices() {
        return devices.toArray(new Device[0]);
    }

    @Override
    public void addSendingTransfer(Transfer transfer) {

    }

    @Override
    public void refreshSendingData() {

    }

    @Override
    public void addReceptionTransfer(Transfer transfer) {

    }

    @Override
    public void refreshReceptionsData() {

    }

    @Override
    public File getDownloadsDirectory() {
        return SystemUtils.getDownloadsDirectory();
    }

    @Override
    public void close() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presenter.onDestroy();
            }
        });
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
    }
}
