package com.afa.devicesfiletransfer.framework;

import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.domain.model.TransferFile;
import com.afa.devicesfiletransfer.framework.database.entities.TransferEntity;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;

public class ModelMapperFactory {
    private static ModelMapper modelMapper;
    private static ModelMapper internalModelMapper = createModelMapperObject();

    public static ModelMapper getInstance() {
        if (modelMapper != null) {
            return modelMapper;
        }

        modelMapper = createModelMapperObject();
        modelMapper.addConverter(createTransferToTransferEntityConverter());
        modelMapper.addConverter(createTransferEntityToTransferConverter());

        return modelMapper;
    }

    private static ModelMapper createModelMapperObject() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

    private static Converter<Transfer, TransferEntity> createTransferToTransferEntityConverter() {
        return new Converter<Transfer, TransferEntity>() {
            @Override
            public TransferEntity convert(MappingContext<Transfer, TransferEntity> context) {
                Transfer transfer = context.getSource();
                TransferEntity transferEntity = internalModelMapper.map(transfer, TransferEntity.class);
                transferEntity.setFileName(transfer.getFile().getName());
                transferEntity.setFilePath(transfer.getFile().getPath());

                return transferEntity;
            }
        };
    }

    private static Converter<TransferEntity, Transfer> createTransferEntityToTransferConverter() {
        return new Converter<TransferEntity, Transfer>() {
            @Override
            public Transfer convert(MappingContext<TransferEntity, Transfer> context) {
                TransferEntity transferEntity = context.getSource();
                Transfer transfer = internalModelMapper.map(transferEntity, Transfer.class);

                TransferFile transferFile = TransferFileFactory
                        .getFromPath(transferEntity.getFilePath(), transferEntity.getFileName());
                transfer.setFile(transferFile);

                return transfer;
            }
        };
    }
}
