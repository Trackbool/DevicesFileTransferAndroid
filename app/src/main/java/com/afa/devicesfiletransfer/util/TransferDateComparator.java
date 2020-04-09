package com.afa.devicesfiletransfer.util;

import com.afa.devicesfiletransfer.domain.model.Transfer;

import java.util.Comparator;
import java.util.Date;

public class TransferDateComparator implements Comparator<Transfer> {
    private boolean ascending;

    public TransferDateComparator() {
        ascending = false;
    }

    public TransferDateComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Transfer t1, Transfer t2) {
        Date d1 = t1.getDate();
        Date d2 = t2.getDate();

        if (ascending) {
            return d2.compareTo(d1);
        } else {
            return d1.compareTo(d2);
        }
    }
}
