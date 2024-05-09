package com.payrix.payrixsdkdemo.ota;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.payrix.payrixsdk.PayDevice;
import com.payrix.payrixsdkdemo.R;

import java.util.List;

public class DeviceListingAdapter extends RecyclerView.Adapter<DeviceListingAdapter.DeviceListingViewHolder> {
    private List<PayDevice> payDeviceList;
    private OnDeviceListingListener deviceListingListener;

    public DeviceListingAdapter(List<PayDevice> payDevices, OnDeviceListingListener deviceListener) {
        this.payDeviceList = payDevices;
        this.deviceListingListener = deviceListener;
    }

    @NonNull
    @Override
    public DeviceListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ota_device_listing_card, parent, false);
        return new DeviceListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListingViewHolder holder, int position) {
        PayDevice payDevice = payDeviceList.get(position);
        if (payDevice != null) {
            holder.deviceNameField.setText(payDevice.deviceSerial);
            holder.cardView.setOnClickListener(view -> {
                deviceListingListener.onDevice(payDevice);
            });
        }
    }

    @Override
    public int getItemCount() {
        return (payDeviceList == null ? 0 :  payDeviceList.size());
    }

    public static class DeviceListingViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final AppCompatTextView deviceNameField;

        public DeviceListingViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card);
            deviceNameField = itemView.findViewById(R.id.device_name);
        }
    }

    interface OnDeviceListingListener {
        void onDevice(PayDevice payDevice);
    }
}
