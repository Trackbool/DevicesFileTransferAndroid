package com.afa.devicesfiletransfer.view.transfer.sender;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;

import java.io.File;

public interface SendTransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void browseFile();

        void showFileAttachedName(String name);

        Device[] getSelectedDevices();

        void addSendingTransfer(Transfer transfer);

        void refreshSendingData();

        void close();
    }

    interface Presenter {
        void onBrowseFileButtonClicked();

        void onFileAttached(File file);

        void onSendFileButtonClicked();

        void onDestroy();
    }
}
