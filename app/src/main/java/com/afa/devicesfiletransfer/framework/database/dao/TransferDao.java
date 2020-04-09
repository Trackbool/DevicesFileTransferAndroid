package com.afa.devicesfiletransfer.framework.database.dao;

import com.afa.devicesfiletransfer.framework.database.entities.TransferEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TransferDao {
    @Query("SELECT * FROM transfer")
    List<TransferEntity> getTransfers();

    @Query("SELECT * FROM transfer ORDER BY dateTime(date) DESC LIMIT :max")
    List<TransferEntity> getLastTransfers(int max);

    @Insert
    void addTransfer(TransferEntity transfer);
}
