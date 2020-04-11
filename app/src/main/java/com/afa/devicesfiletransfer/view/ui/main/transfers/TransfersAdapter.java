package com.afa.devicesfiletransfer.view.ui.main.transfers;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Transfer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransfersAdapter extends RecyclerView.Adapter<TransfersAdapter.TransfersViewHolder> {

    private List<Transfer> transfers;

    public TransfersAdapter() {
        transfers = new ArrayList<>();
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
        holder.icon.setImageResource(R.drawable.mobile_icon);
        holder.fileName.setText(transfer.getFileName());
        holder.foreignDevice.setText(transfer.getDeviceName());
        holder.percentage.setText(transfer.getProgressPercentage());
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        holder.date.setText(simpleDateFormat.format(transfer.getDate()));
        holder.status.setText(transfer.getStatus().getValue());
        holder.inOutArrow.setImageResource(
                transfer.isIncoming() ? R.drawable.incoming_arrow : R.drawable.outgoing_arrow);
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    public void refreshData() {
        notifyDataSetChanged();
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
        }
    }
}
