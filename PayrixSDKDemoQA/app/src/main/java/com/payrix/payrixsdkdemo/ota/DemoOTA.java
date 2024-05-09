package com.payrix.payrixsdkdemo.ota;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bbpos.bbdevice.ota.BBDeviceControllerNotSetException;
import com.bbpos.bbdevice.ota.BBDeviceControllerNotSupportOTAException;
import com.bbpos.bbdevice.ota.BBDeviceNotConnectedException;
import com.bbpos.bbdevice.ota.BBDeviceOTAController;
import com.bbpos.bbdevice.ota.NoInternetConnectionException;
import com.bbpos.bbdevice.ota.OTAServerURLNotSetException;
import com.payrix.payrixsdk.PayrixOTA;
import com.payrix.payrixsdk.PayrixOTACallbacks;
import com.payrix.payrixsdk.PayrixOTAConfigData;
import com.payrix.payrixsdkdemo.R;
import com.payrix.payrixsdkdemo.SharedUtilities;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class DemoOTA extends AppCompatActivity implements PayrixOTACallbacks, OnDemoOTAListener {
    private Fragment currentFragment;
    private ProgressBar progressBar;
    private final PayrixOTA payrixOTA = PayrixOTA.getInstance(this);
    private final String TAG = DemoOTA.class.getSimpleName();
    private Hashtable<String, String> currentDeviceInfo;
    private TextView toolbarTitle;

    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();
    private PayrixOTAConfigData bbPOSConfigData, otaConfigData;
    private AppCompatImageView infoBtn;

    //setting the Device Settings version fetched from API
    private String currentDeviceSettingVersion = "";
    //setting the Device Firmware version fetched from API
    private String currentFirmwareVersion = "";
    //setting the Device Terminal Settings version fetched from API
    private String currentTerminalSettingVersion = "";
    //setting the Encryption Key fetched from Device Details
    private String currentEncryptionKey = "";

    public static final int CONFIG_UPDATE = 0;
    public static final int FIRMWARE_UPDATE = 1;
    public static final int KEY_PROFILE_UPDATE = 2;

    private ProgressBar updateProgressView;
    private AppCompatTextView progressLabel;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_ota);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Payrix SDK Demo App");
        setSupportActionBar(toolbar);

        infoBtn = findViewById(R.id.info_btn);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_black_24);
        toolbar.setElevation(10.0f);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        infoBtn.setOnClickListener(view -> {
            if (currentDeviceInfo != null) {
                Intent intent = new Intent(DemoOTA.this, DeviceDetailsActivity.class);
                intent.putExtra("deviceInfo", convertHashTableToHashMap(currentDeviceInfo));
                startActivity(intent);
            }
        });

        progressBar = findViewById(R.id.loading);
        showFragment(new DeviceListingFragment());
    }

    /**
     * Converts Hashtable to Hashmap for easy pass between Activity
     * @param data - Hashtable to be converted to Hashmap
     * @return - Hashmap after a successful conversion
     */
    private HashMap<String, String> convertHashTableToHashMap(Hashtable<String, String> data) {
        HashMap<String, String> retHashMap = new HashMap<>();
        for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
            String key = String.valueOf(stringStringEntry.getKey());
            String value = String.valueOf(stringStringEntry.getValue());
            retHashMap.put(key, value);
        }
        return retHashMap;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        this.currentFragment = fragment;
        transaction.commit();
    }

    @Override
    public void didReceiveRemoteKeyInjectionResult(boolean success, BBDeviceOTAController.OTAResult otaResult, String s) {
        Log.d(TAG, "didReceiveRemoteKeyInjectionResult");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (success){
            sharedUtils.showMessage(this, "Encryption Key Updated successfully.", "");
        } else {
            sharedUtils.showMessage(this, "Encryption Key NOT Updated.", s);
        }
    }

    @Override
    public void didReceiveRemoteFirmwareUpdate(boolean success, BBDeviceOTAController.OTAResult otaResult, String s) {
        Log.d(TAG, "didReceiveRemoteFirmwareUpdate");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (success){
            sharedUtils.showMessage(this, "Firmware Updated successfully.", "");
        } else {
            sharedUtils.showMessage(this, "Firmware NOT Updated.", s);
        }
    }

    @Override
    public void didReceiveRemoteConfigUpdate(boolean success, BBDeviceOTAController.OTAResult otaResult, String s) {
        Log.d(TAG, "didReceiveRemoteConfigUpdate");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (success){
            sharedUtils.showMessage(this, "Configuration Updated successfully.", "");
        } else {
            sharedUtils.showMessage(this, "Configuration NOT Updated.", s);
        }
    }

    /**
     * STEP 6
     * Update the firmware, key profile and config here. Check line 178 class DeviceUpdateFragment method updateDeviceLatestVersionLabel() was called to show device latest data/info gotten via OTA.
     * We also use the stored keys in STEP 4 to check if update is needed for firmware, key profile and config
     * @param success
     * @param otaResult
     * @param payrixOTAConfigData
     * @param payrixBBPOSConfigData
     */
    @Override
    public void didReceiveTargetVersionResult(boolean success, BBDeviceOTAController.OTAResult otaResult, PayrixOTAConfigData payrixOTAConfigData, PayrixOTAConfigData payrixBBPOSConfigData) {
        Log.d(TAG, "didReceiveTargetVersionResult");
        progressBar.setVisibility(View.GONE);
        if (otaResult == BBDeviceOTAController.OTAResult.FAILED) {
            sharedUtils.showMessage(this, "Error", "");
            return;
        }

        otaConfigData = payrixOTAConfigData;
        bbPOSConfigData = payrixBBPOSConfigData;
        if (currentFragment instanceof DeviceUpdateFragment) {
            DeviceUpdateFragment duf = (DeviceUpdateFragment) currentFragment;
            duf.updateDeviceLatestVersionLabel(payrixOTAConfigData.deviceSettingVersion, payrixOTAConfigData.firmwareVersion, payrixOTAConfigData.encryptionKey);
        }

        // This checks if config needs update or not
        doHideUpdateConfig();

        // This checks if firmware needs update or not
        doHideUpdateFirmware();

        //This checks if key profile / encryption key needs update or not
        doHideUpdateEncryptionKey();
    }

    @Override
    public void didReceiveOTAProgress(double v) {
        Log.d(TAG, "didReceiveOTAProgress");
        if (updateProgressView != null && progressLabel != null) {
            updateProgressView.setProgress((int)v);

            progressLabel.setText(String.format(Locale.getDefault(), "%.1f", v));
        }
    }

    /**
     * After pulling config data via OTA update UI if update is needed or not
     */
    private void doHideUpdateConfig() {
        if (currentTerminalSettingVersion.equalsIgnoreCase(otaConfigData.terminalSettingVersion) || otaConfigData.terminalSettingVersion.isEmpty()) {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleConfigFrame(true);
            }
        } else {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleConfigFrame(false);
            }
        }
    }

    /**
     * After pulling firmware data via OTA update UI if update is needed or not
     */
    private void doHideUpdateFirmware() {
        if (currentFirmwareVersion.equalsIgnoreCase(otaConfigData.firmwareVersion) || otaConfigData.firmwareVersion.isEmpty()) {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleFirmwareFrame(true);
            }
        } else {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleFirmwareFrame(false);
            }
        }
    }

    /**
     * NOTE: You will notice PayrixTargetKeyProfileName is no more in use.
     * We now test PayrixCurrentKeyProfileName against the encryptionKey gotten from STEP 6
     * After pulling key profile data via OTA update UI if update is needed or not
     */
    private void doHideUpdateEncryptionKey() {
        if (otaConfigData.encryptionKey.isEmpty() || otaConfigData.encryptionKey.equalsIgnoreCase("Profile Not Supported by Payrix")) {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleKeyProfileFrame(true);
            }
        } else if (!currentEncryptionKey.equalsIgnoreCase(otaConfigData.encryptionKey)) {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).toggleKeyProfileFrame(false);
            }
        } else {
            if (currentFragment instanceof DeviceUpdateFragment) {
                ((DeviceUpdateFragment) currentFragment).updateKeyProfileFrame();
            }
        }
    }

    /**
     * This is a dialog that display update progress when updating config, firmware or key profile
     * @param title - The title of the current task (config, firmware or key profile) to be performed.
     */
    private void showUpdateProgressDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.custom_ota_update_progress_dialog, null);
        builder.setView(view);

        AppCompatTextView titleLabel = view.findViewById(R.id.dialog_title);
        progressLabel = view.findViewById(R.id.progress_label);
        updateProgressView = view.findViewById(R.id.update_progress);

        titleLabel.setText(title);

        alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * STEP 5
     * Start OTA and listen to it’s callbacks. The salient callbacks for updating the device are:
     * didReceiveRemoteKeyInjectionResult(), didReceiveRemoteFirmwareUpdate(), didReceiveRemoteConfigUpdate() and didReceiveTargetVersionResult()
     * On didReceiveTargetVersionResult(), check if you need to update firmware, keyprofile and config.
     * Goto to {@link #didReceiveTargetVersionResult(boolean, BBDeviceOTAController.OTAResult, PayrixOTAConfigData, PayrixOTAConfigData)} for STEP 6
     * @throws BBDeviceControllerNotSupportOTAException
     * @throws OTAServerURLNotSetException
     * @throws BBDeviceNotConnectedException
     * @throws NoInternetConnectionException
     * @throws BBDeviceControllerNotSetException
     */
    @Override
    public void startOTA() throws BBDeviceControllerNotSupportOTAException, OTAServerURLNotSetException, BBDeviceNotConnectedException, NoInternetConnectionException, BBDeviceControllerNotSetException {
        Log.d(TAG, "Starting OTA");
        payrixOTA.doOTAStartup(this);
        payrixOTA.doGetTargetVersion();
    }

    /**
     * STEP 4
     * Get and process the device info. Store important device info you need.
     * To update device later on, you will need store these keys: firmwareVersion, terminalSettingVersion and PayrixCurrentKeyProfileName.
     *  After, then you can show the Device update screen for updating key profile, firmware etc
     * @param deviceInfo - Returned device dictionary
     */
    @Override
    public void gotoDeviceUpdate(Hashtable<String, String> deviceInfo) {
        currentDeviceInfo = deviceInfo;
        currentFirmwareVersion = deviceInfo.get("firmwareVersion");
        currentTerminalSettingVersion = deviceInfo.get("terminalSettingVersion");
        String deviceSettingVersion = deviceInfo.get("deviceSettingVersion");
        currentEncryptionKey = deviceInfo.get("PayrixCurrentKeyProfileName");
        String serialNumber = deviceInfo.get("serialNumber");

        infoBtn.setVisibility(View.VISIBLE);
        if (serialNumber != null) {
            toolbarTitle.setText(serialNumber);
        }

        if (currentEncryptionKey == null) {
            currentEncryptionKey = "";
        }

        //Show or display device update fragment after a successful BT connection
        DeviceUpdateFragment deviceUpdateFragment = DeviceUpdateFragment.newInstance(currentTerminalSettingVersion, currentFirmwareVersion, currentEncryptionKey);
        showFragment(deviceUpdateFragment);
    }

    @Override
    public void doUpdateConfig() {
        try {
            payrixOTA.doOTAConfigUpdate(bbPOSConfigData.deviceSettingVersion, bbPOSConfigData.terminalSettingVersion);
            showUpdateProgressDialog("Updating Configuration");
        } catch (OTAServerURLNotSetException | BBDeviceNotConnectedException | NoInternetConnectionException | BBDeviceControllerNotSetException e) {
            e.printStackTrace();
            sharedUtils.showMessage(this, "OTA Error", e.getMessage());
        }
    }

    @Override
    public void doUpdateFirmware() {
        try {
            payrixOTA.doOTAFirmwareUpdate(bbPOSConfigData.firmwareVersion);
            showUpdateProgressDialog("Updating Firmware");
        } catch (OTAServerURLNotSetException | BBDeviceNotConnectedException | NoInternetConnectionException | BBDeviceControllerNotSetException e) {
            e.printStackTrace();
            sharedUtils.showMessage(this, "OTA Error", e.getMessage());
        }
    }

    @Override
    public void doUpdateKeyInjection() {
        try {
            payrixOTA.doOTAKeyInjection(bbPOSConfigData.encryptionKey);
            showUpdateProgressDialog("Updating Encryption Key");
        } catch (OTAServerURLNotSetException | BBDeviceNotConnectedException | NoInternetConnectionException | BBDeviceControllerNotSetException e) {
            e.printStackTrace();
            sharedUtils.showMessage(this, "OTA Error", e.getMessage());
        }
    }
}