package com.payrix.payrixsdkdemo.ota;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.TextView;

import com.payrix.payrixsdkdemo.R;

import java.util.HashMap;
import java.util.Hashtable;

public class DeviceDetailsActivity extends AppCompatActivity {
    HashMap<String, String> deviceInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        deviceInfo = (HashMap<String, String>) getIntent().getSerializableExtra("deviceInfo");
        if (deviceInfo == null) {
            finish();
            return;
        }

        String serialNumber = deviceInfo.get("serialNumber");
        if (serialNumber != null) {
            toolbarTitle.setText(serialNumber);
        }
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        RecyclerView recyclerView = findViewById(R.id.device_detail_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DeviceDetailAdapter adapter = new DeviceDetailAdapter(deviceInfo);
        recyclerView.setAdapter(adapter);
    }
}