package com.afa.devicesfiletransfer.view.ui.main.devices;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder> {

    private List<Device> devices;
    private Callback callback;
    private Set<Integer> selectedPositions;

    public DevicesAdapter(Callback callback) {
        this.devices = new ArrayList<>();
        this.callback = callback;
        this.selectedPositions = new HashSet<>();
    }

    public void setDevices(List<Device> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }

    public void addDevice(Device device) {
        this.devices.add(device);
        notifyDataSetChanged();
    }

    public List<Device> getSelectedDevices() {
        List<Device> selectedDevices = new ArrayList<>();
        for (Integer position : selectedPositions) {
            selectedDevices.add(devices.get(position));
        }

        return selectedDevices;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void deselectDevices() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void clearDevices() {
        this.devices.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DevicesAdapter.DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devices_list, parent, false);
        return new DevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesAdapter.DevicesViewHolder holder, int position) {
        int backgroundColor = selectedPositions.contains(position) ? R.color.selectedItemList : Color.TRANSPARENT;
        holder.itemView.setBackgroundResource(backgroundColor);

        Device device = devices.get(position);
        holder.icon.setImageResource(getDeviceIcon(device));
        holder.name.setText(device.getName());
        holder.os.setText(device.getOs());
        holder.ipAddress.setText(device.getIpAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DevicesViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView os;
        TextView ipAddress;

        DevicesViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.deviceIconImageView);
            name = itemView.findViewById(R.id.deviceNameTextView);
            os = itemView.findViewById(R.id.deviceOsTextView);
            ipAddress = itemView.findViewById(R.id.deviceIpAddressTextView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) return false;

                    selectedPositions.add(getAdapterPosition());
                    notifyItemChanged(getAdapterPosition());

                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                    Device selectedDevice = devices.get(getAdapterPosition());

                    if (selectedPositions.contains(getAdapterPosition())) {
                        selectedPositions.remove(getAdapterPosition());
                        notifyDataSetChanged();
                        callback.onItemDeselected(selectedDevice);
                    } else if (selectedPositions.size() > 0) {
                        selectedPositions.add(getAdapterPosition());
                        notifyItemChanged(getAdapterPosition());
                        callback.onItemSelected(selectedDevice);
                    } else {
                        callback.onClick(selectedDevice);
                    }
                }
            });
        }
    }

    private int getDeviceIcon(Device device) {
        String os = device.getOs().replace(" ", "").toLowerCase();
        if (os.contains("android") && !os.contains("tv") || os.contains("ios") || os.contains("phone")
                || os.contains("mobile")) {
            return R.drawable.mobile_icon;
        }

        return R.drawable.pc_icon;
    }

    public interface Callback {
        void onClick(Device device);

        void onItemSelected(Device device);

        void onItemDeselected(Device device);
    }
}
