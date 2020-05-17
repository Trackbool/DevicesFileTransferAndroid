package com.afa.devicesfiletransfer.view.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.domain.model.TransferFileFactory;
import com.afa.devicesfiletransfer.framework.TransferFileUri;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderReceiver;
import com.afa.devicesfiletransfer.services.transfer.sender.FileSenderServiceExecutor;
import com.afa.devicesfiletransfer.util.file.FileUtils;
import com.afa.devicesfiletransfer.view.components.LabeledImageView;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderReceiverImpl;
import com.afa.devicesfiletransfer.view.framework.services.transfer.sender.FileSenderServiceExecutorImpl;
import com.afa.devicesfiletransfer.view.model.AlertModel;
import com.afa.devicesfiletransfer.view.model.ErrorModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.sender.SendTransferViewModel;
import com.afa.devicesfiletransfer.view.viewmodels.transfer.sender.SendTransferViewModelFactory;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class SendFileActivity extends BaseActivity {
    private SendTransferViewModel sendTransferViewModel;
    private List<Device> devices;
    private TextView receiverDevicesTextView;
    private TextView noFilesAttachedTextView;
    private HorizontalScrollView fileImagesScrollView;
    private LinearLayout fileImagesContainer;
    private Button sendFileButton;

    private static final int BROWSE_FILES_RESULT_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.send_files_title));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        devices = Objects.requireNonNull(
                getIntent().getExtras()).getParcelableArrayList("devicesList");

        initViews();
        initializeSendTransferViewModel();
        displayTargetDevices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        final Button attachFileButton = findViewById(R.id.attachFileButton);
        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseFile();
            }
        });
        receiverDevicesTextView = findViewById(R.id.receiverDevicesTextView);
        noFilesAttachedTextView = findViewById(R.id.noFilesAttachedTextView);
        fileImagesScrollView = findViewById(R.id.fileImagesScrollView);
        fileImagesContainer = findViewById(R.id.fileImagesContainer);
        sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTransferViewModel.sendFiles(devices);
                attachFileButton.setClickable(false);
                sendFileButton.setClickable(false);
                finish();
            }
        });
    }

    private void initializeSendTransferViewModel() {
        FileSenderServiceExecutor fileSenderExecutor =
                new FileSenderServiceExecutorImpl(getApplicationContext());
        FileSenderReceiver fileSenderReceiver =
                new FileSenderReceiverImpl(getApplicationContext());
        sendTransferViewModel = new ViewModelProvider(this,
                new SendTransferViewModelFactory(fileSenderExecutor, fileSenderReceiver))
                .get(SendTransferViewModel.class);

        sendTransferViewModel.getAttachedFiles().observe(this, new Observer<List<TransferFile>>() {
            @Override
            public void onChanged(List<TransferFile> transferFiles) {
                showFileImagesInGallery(transferFiles);
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
    }

    private void displayTargetDevices() {
        StringBuilder receivers = new StringBuilder();
        for (Device device : devices) {
            receivers.append(device.getName()).append(", ");
        }
        String receiversText = receivers.toString().substring(0, receivers.length() - 2);
        receiverDevicesTextView.setText(receiversText);
    }

    private void browseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, BROWSE_FILES_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BROWSE_FILES_RESULT_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                List<TransferFile> files = new ArrayList<>();
                if (data.getClipData() != null) {
                    int filesCount = data.getClipData().getItemCount();
                    for (int i = 0; i < filesCount; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        files.add(TransferFileFactory.getFromUri(uri));
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    files.add(TransferFileFactory.getFromUri(uri));
                }

                if (!files.isEmpty()) {
                    sendTransferViewModel.attachFiles(files);
                    sendFileButton.setEnabled(true);
                    noFilesAttachedTextView.setVisibility(View.INVISIBLE);
                } else {
                    sendFileButton.setEnabled(false);
                    noFilesAttachedTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void showFileImagesInGallery(List<TransferFile> transferFiles) {
        fileImagesContainer.removeAllViews();

        for (TransferFile file : transferFiles) {
            ImageView imageView = new ImageView(SendFileActivity.this);
            RequestCreator picassoCreator = loadPicassoImageBasedOnFileType(file);

            final int DEFAULT_IMAGE_DIMENSION = 900;
            final int containerWidth = fileImagesScrollView.getWidth();
            if (transferFiles.size() == 1 && containerWidth > 0) {
                picassoCreator.resize(containerWidth, DEFAULT_IMAGE_DIMENSION);
            } else {
                picassoCreator.resize(DEFAULT_IMAGE_DIMENSION, DEFAULT_IMAGE_DIMENSION);
            }
            picassoCreator.centerCrop().into(imageView);

            LabeledImageView labeledImageView = new LabeledImageView(
                    SendFileActivity.this, imageView);

            final int MAX_FILE_NAME_LENGTH = 22;
            String truncatedFileName = getTruncatedFileName(file.getName(), MAX_FILE_NAME_LENGTH);
            labeledImageView.getLabelText().setText(truncatedFileName);
            fileImagesContainer.addView(labeledImageView);
        }
    }

    private RequestCreator loadPicassoImageBasedOnFileType(TransferFile file) {
        RequestCreator picassoCreator;
        if (FileUtils.isImage(file.getName()) && file instanceof TransferFileUri) {
            TransferFileUri fileUri = (TransferFileUri) file;
            picassoCreator = Picasso.get().load(fileUri.getUri());
        } else if (FileUtils.isAudio(file.getName())) {
            picassoCreator = Picasso.get().load(R.drawable.audio_icon); //TODO: Change image
        } else if (FileUtils.isVideo(file.getName())) {
            picassoCreator = Picasso.get().load(R.drawable.video_icon); //TODO: Change image
        } else {
            picassoCreator = Picasso.get().load(R.drawable.file_icon); //TODO: Change image
        }

        return picassoCreator;
    }

    private String getTruncatedFileName(String fileName, int maxLength) {
        String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);
        if (fileNameWithoutExtension.length() > maxLength) {
            String fileExtension = FileUtils.getFileExtension(fileName);
            fileName = fileNameWithoutExtension.substring(0, maxLength) + "..." +
                    (fileExtension.length() <= 4? fileExtension : "");
        }

        return fileName;
    }

    @Override
    protected void onPause() {
        sendTransferViewModel.onHideView();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        sendTransferViewModel.onShowView();
        super.onPostResume();
    }
}
