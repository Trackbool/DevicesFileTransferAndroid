package com.afa.devicesfiletransfer.framework.repository;

import android.content.Context;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.repository.TransfersRepository;
import com.afa.devicesfiletransfer.framework.database.TransferDatabase;
import com.afa.devicesfiletransfer.framework.database.dao.TransferDao;
import com.afa.devicesfiletransfer.framework.database.entities.TransferEntity;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Room;

public class TransfersRoomDatabaseRepository implements TransfersRepository {
    private final static String TRANSFER_DATABASE_NAME = "transfer";
    private final ModelMapper modelMapper;
    private final TransferDao transferDao;

    public TransfersRoomDatabaseRepository(Context context) {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Context applicationContext = context.getApplicationContext();
        TransferDatabase database = Room.databaseBuilder(
                applicationContext, TransferDatabase.class, TRANSFER_DATABASE_NAME)
                .build();
        transferDao = database.getTransferDao();
    }

    @Override
    public void getTransfers(GetTransfersCallback callback) {
        List<TransferEntity> transferEntities = transferDao.getTransfers();
        List<Transfer> transfers = convertTransferEntityList(transferEntities);
        callback.onSuccess(transfers);
    }

    @Override
    public void getLastTransfers(int max, GetTransfersCallback callback) {
        List<TransferEntity> transferEntities = transferDao.getLastTransfers(max);
        List<Transfer> transfers = convertTransferEntityList(transferEntities);
        callback.onSuccess(transfers);
    }

    @Override
    public void saveTransfer(Transfer transfer, AddTransferCallback callback) {
        TransferEntity transferEntity = modelMapper.map(transfer, TransferEntity.class);
        transferDao.addTransfer(transferEntity);
        callback.onSuccess();
    }

    private List<Transfer> convertTransferEntityList(List<TransferEntity> transferEntities) {
        final List<Transfer> transfers = new ArrayList<>();

        for (TransferEntity transferEntity : transferEntities) {
            Transfer transfer = modelMapper.map(transferEntity, Transfer.class);
            transfers.add(transfer);
        }

        return transfers;
    }
}
