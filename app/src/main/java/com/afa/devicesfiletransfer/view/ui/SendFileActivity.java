package com.afa.devicesfiletransfer.view.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;
import com.afa.devicesfiletransfer.model.TransferFile;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.model.TransferFileImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.presenters.transfer.sender.SendTransferContract;
import com.afa.devicesfiletransfer.view.presenters.transfer.sender.SendTransferPresenter;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

public class SendFileActivity extends AppCompatActivity implements SendTransferContract.View {

    private SendTransferContract.Presenter sendTransferPresenter;
    private List<Device> devices;
    private TextView attachedFileNameTextView;

    private static final int BROWSE_FILE_RESULT_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        devices = Objects.requireNonNull(
                getIntent().getExtras()).getParcelableArrayList("devicesList");

        FileSenderServiceExecutor fileSenderExecutor =
                new FileSenderServiceExecutorImpl(getApplicationContext());
        sendTransferPresenter = new SendTransferPresenter(this, fileSenderExecutor);
        Button attachFileButton = findViewById(R.id.attachFileButton);
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransferPresenter.onBrowseFileButtonClicked();
            }
        });
        attachedFileNameTextView = findViewById(R.id.attachedFileNameTextView);
        Button sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransferPresenter.onSendFileButtonClicked();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("attachedFileName", String.valueOf(attachedFileNameTextView.getText()));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        attachedFileNameTextView.setText(savedInstanceState.getString("attachedFileName"));
        super.onRestoreInstanceState(savedInstanceState);
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
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, BROWSE_FILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BROWSE_FILE_RESULT_CODE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    TransferFile file = new TransferFileImpl(getApplicationContext(), uri);
                    sendTransferPresenter.onFileAttached(file);
                }
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
    public void close() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendTransferPresenter.onDestroy();
            }
        });
    }

    @Override
    protected void onDestroy() {
        sendTransferPresenter.onDestroy();
        super.onDestroy();
    }
}
