package com.afa.devicesfiletransfer.framework.database;

import com.afa.devicesfiletransfer.framework.database.dao.TransferDao;
import com.afa.devicesfiletransfer.framework.database.entities.TransferEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TransferEntity.class}, version = 1)
public abstract class TransferDatabase extends RoomDatabase {
    public abstract TransferDao getTransferDao();
}
