package com.afa.devicesfiletransfer.view.transfer.receiver;

import com.afa.devicesfiletransfer.model.Transfer;

import java.io.File;

public interface ReceiveTransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void addReceptionTransfer(Transfer transfer);

        void refreshReceptionsData();

        File getDownloadsDirectory();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDestroy();
    }
}
