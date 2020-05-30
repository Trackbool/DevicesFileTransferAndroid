package com.afa.devicesfiletransfer.view.ui.main.transfers;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Transfer;
import com.afa.devicesfiletransfer.util.file.FileType;
import com.afa.devicesfiletransfer.util.file.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransfersAdapter extends RecyclerView.Adapter<TransfersAdapter.TransfersViewHolder> {

    private final List<Transfer> transfers;
    private Callback callback;

    public TransfersAdapter() {
        transfers = new ArrayList<>();
    }

    public TransfersAdapter(Callback callback) {
        this();
        this.callback = callback;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers.clear();
        this.transfers.addAll(transfers);
        notifyDataSetChanged();
    }

    public void addTransfer(Transfer transfer) {
        this.transfers.add(transfer);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransfersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfers_list, parent, false);
        return new TransfersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransfersViewHolder holder, int position) {
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        Transfer transfer = transfers.get(position);
        holder.icon.setImageResource(getMediaIcon(transfer.getFile().getName()));
        holder.fileName.setText(transfer.getFile().getName());
        holder.foreignDevice.setText(transfer.getDeviceName());
        holder.percentage.setText(transfer.getProgressPercentage());
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        holder.date.setText(simpleDateFormat.format(transfer.getDate()));
        holder.status.setText(transfer.getStatus().getValue());
        holder.inOutArrow.setImageResource(
                transfer.isIncoming() ? R.drawable.incoming_arrow : R.drawable.outgoing_arrow);
    }

    private int getMediaIcon(String fileName) {
        FileType fileType = FileUtils.getFileType(fileName);

        switch (fileType) {
            case AUDIO:
                return R.drawable.audio_icon;
            case IMAGE:
                return R.drawable.image_icon;
            case VIDEO:
                return R.drawable.video_icon;
            default:
                return R.drawable.file_icon;
        }
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    public void refresh(Transfer transfer) {
        int position = transfers.indexOf(transfer);
        notifyItemChanged(position, transfer);
    }

    class TransfersViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView fileName;
        TextView foreignDevice;
        TextView date;
        TextView percentage;
        TextView status;
        ImageView inOutArrow;

        TransfersViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.transferIconImageView);
            fileName = itemView.findViewById(R.id.transferFileTextView);
            foreignDevice = itemView.findViewById(R.id.foreignDeviceTextView);
            date = itemView.findViewById(R.id.transferDate);
            percentage = itemView.findViewById(R.id.transferPercentage);
            status = itemView.findViewById(R.id.transferStatus);
            inOutArrow = itemView.findViewById(R.id.inOutArrowImageView);

            if (callback != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                        Transfer clickedTransfer = transfers.get(getAdapterPosition());
                        callback.onClick(clickedTransfer);
                    }
                });
            }
        }
    }

    public interface Callback {
        void onClick(Transfer transfer);
    }
}
