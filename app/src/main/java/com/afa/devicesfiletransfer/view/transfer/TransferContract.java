package com.afa.devicesfiletransfer.view.transfer;

import com.afa.devicesfiletransfer.model.Device;
import com.afa.devicesfiletransfer.model.Transfer;

import java.io.File;

public interface TransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void browseFile();

        void showFileAttachedName(String name);

        Device[] getSelectedDevices();

        void addSendingTransfer(Transfer transfer);

        void refreshSendingData();

        void addReceptionTransfer(Transfer transfer);

        void refreshReceptionsData();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onBrowseFileButtonClicked();

        void onFileAttached(File file);

        void onSendFileButtonClicked();

        void onDestroy();
    }
}
