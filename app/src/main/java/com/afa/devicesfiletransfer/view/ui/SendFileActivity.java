package com.afa.devicesfiletransfer.view.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.Pair;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.domain.model.TransferFileFactory;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderReceiverImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.model.AlertModel;
import com.afa.devicesfiletransfer.view.model.ErrorModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.sender.SendTransferViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.sender.SendTransferViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class SendFileActivity extends AppCompatActivity {
    private SendTransferViewModel sendTransferViewModel;
    private List<Device> devices;
    private TextView attachedFileNameTextView;

    private static final int BROWSE_FILE_RESULT_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        devices = Objects.requireNonNull(
                getIntent().getExtras()).getParcelableArrayList("devicesList");

        Button attachFileButton = findViewById(R.id.attachFileButton);
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseFile();
            }
        });
        attachedFileNameTextView = findViewById(R.id.attachedFileNameTextView);
        Button sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransferViewModel.sendFile(devices);
            }
        });

        initializeSendTransferViewModel();
        sendTransferViewModel.onStart();
    }

    private void initializeSendTransferViewModel() {
        FileSenderServiceExecutor fileSenderExecutor =
                new FileSenderServiceExecutorImpl(getApplicationContext());
        FileSenderReceiver fileSenderReceiver =
                new FileSenderReceiverImpl(getApplicationContext());
        sendTransferViewModel = new ViewModelProvider(this,
                new SendTransferViewModelFactory(fileSenderExecutor, fileSenderReceiver))
                .get(SendTransferViewModel.class);

        sendTransferViewModel.getAttachedFile().observe(this, new Observer<TransferFile>() {
            @Override
            public void onChanged(TransferFile transferFile) {
                attachedFileNameTextView.setText(transferFile.getName());
            }
        });
        sendTransferViewModel.getAlertEvent().observe(this, new Observer<AlertModel>() {
            @Override
            public void onChanged(AlertModel alertModel) {
                showAlert(alertModel.getTitle(), alertModel.getMessage());
            }
        });
        sendTransferViewModel.getErrorEvent().observe(this, new Observer<ErrorModel>() {
            @Override
            public void onChanged(ErrorModel errorModel) {
                showError(errorModel.getTitle(), errorModel.getMessage());
            }
        });
        sendTransferViewModel.getOnTransferSucceededEvent().observe(this,
                new Observer<Pair<Transfer, TransferFile>>() {
                    @Override
                    public void onChanged(Pair<Transfer, TransferFile> transferTransferFilePair) {
                        Transfer transfer = transferTransferFilePair.getLeft();
                        showAlert("File sent", "The file " + transfer.getFile().getName());
                    }
                });
    }

    private void browseFile() {
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
                    TransferFile file = TransferFileFactory.getFromUri(getApplicationContext(), uri);
                    sendTransferViewModel.attachFile(file);
                }
            }
        }
    }

    public void showError(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(SendFileActivity.this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }

    public void showAlert(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(SendFileActivity.this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        sendTransferViewModel.onDestroy();
        super.onDestroy();
    }
}
