package com.payrix.payrixsdkdemo.ota;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.payrix.payrixsdkdemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceDetailAdapter extends RecyclerView.Adapter<DeviceDetailAdapter.DeviceDetailViewHolder> {

    private List<String> keyList;
    private List<String> valueList;

    public DeviceDetailAdapter(HashMap<String, String> hashMap) {
        valueList = new ArrayList<>(hashMap.values());
        keyList = new ArrayList<>(hashMap.keySet());
    }


    @NonNull
    @Override
    public DeviceDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceDetailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_details_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceDetailViewHolder holder, int position) {
        holder.deviceInfoKeyField.setText(keyList.get(position));
        holder.deviceInfoValueField.setText(valueList.get(position));
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    public static class DeviceDetailViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView deviceInfoKeyField;
        private final AppCompatTextView deviceInfoValueField;

        public DeviceDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceInfoKeyField = itemView.findViewById(R.id.device_info_key);
            deviceInfoValueField = itemView.findViewById(R.id.device_info_value);
        }
    }
}
