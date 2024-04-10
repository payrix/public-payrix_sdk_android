package com.payrix.payrixsdkdemo.ota;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bbpos.bbdevice.ota.BBDeviceControllerNotSetException;
import com.bbpos.bbdevice.ota.BBDeviceControllerNotSupportOTAException;
import com.bbpos.bbdevice.ota.BBDeviceNotConnectedException;
import com.bbpos.bbdevice.ota.NoInternetConnectionException;
import com.bbpos.bbdevice.ota.OTAServerURLNotSetException;
import com.payrix.payrixsdkdemo.R;
import com.payrix.payrixsdkdemo.SharedUtilities;


public class DeviceUpdateFragment extends Fragment {
    private final String configInfo = "The Configuration is a set of parameters that reside on the bbPOS device that specifies the requirements that Payrix has when performing transactions. A example is the maximum transaction limit when using such a device. This and many more make up the device configuration.";
    private final String firmwareInfo = "Firmware is special hardware related software that is managed by the hardware manufacturer (bbPOS). Occasionally the hardware manufacturer has minor fixes or enhancements that allow the device to perform better or to meet a specific regulatory requirement.";
    private final String encryptionInfo = "The payment device reader is a highly secured device. Part of that is due to the use of encryption keys. When working with Payrix there are basically 2 types of keys. A Sandbox Key which is used for testing in the Payrix Sandbox environment, and a Live Production Key for use by merchants performing transactions on Payrix's Live Production platform.";

    private AppCompatTextView configVersionField, payrixConfigVersionField;
    private AppCompatTextView firmwareVersionField, payrixFirmwareVersionField;
    private AppCompatTextView keyProfileVersionField, payrixKeyProfileVersionField;

    private LinearLayoutCompat configUpdateFrame, firmwareUpdateFrame, keyProfileUpdateFrame;
    private Button configUpdateBtn, firmwareUpdateBtn, keyProfileUpdateBtn;
    private AppCompatTextView keyProfileRecommendField;

    private final SharedUtilities sharedUtils = SharedUtilities.getInstance();

    private static final String CONFIG_PARAM = "config";
    private static final String FIRMWARE_PARAM = "firmware";
    private static final String KEY_PROFILE_PARAM = "keyProfile";

    private String mConfig;
    private String mFirmware;
    private String mKeyProfile;

    private OnDemoOTAListener mListener;

    public DeviceUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mConfig = getArguments().getString(CONFIG_PARAM);
            mFirmware = getArguments().getString(FIRMWARE_PARAM);
            mKeyProfile = getArguments().getString(KEY_PROFILE_PARAM);
        }
    }

    public static DeviceUpdateFragment newInstance(String config, String firmware, String keyProfile) {
        DeviceUpdateFragment fragment = new DeviceUpdateFragment();
        Bundle args = new Bundle();
        args.putString(CONFIG_PARAM, config);
        args.putString(FIRMWARE_PARAM, firmware);
        args.putString(KEY_PROFILE_PARAM, keyProfile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_update, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configVersionField = view.findViewById(R.id.config_version_title);
        payrixConfigVersionField = view.findViewById(R.id.config_payrix_version_title);
        firmwareVersionField = view.findViewById(R.id.firmware_version_title);
        payrixFirmwareVersionField = view.findViewById(R.id.firmware_payrix_version_title);
        keyProfileVersionField = view.findViewById(R.id.key_profile_version_title);
        payrixKeyProfileVersionField = view.findViewById(R.id.key_profile_payrix_version_title);

        configUpdateFrame = view.findViewById(R.id.config_update_frame);
        firmwareUpdateFrame = view.findViewById(R.id.firmware_update_frame);
        keyProfileUpdateFrame = view.findViewById(R.id.key_profile_update_frame);

        configUpdateBtn = view.findViewById(R.id.configUpdateBtn);
        firmwareUpdateBtn = view.findViewById(R.id.firmwareUpdateBtn);
        keyProfileUpdateBtn = view.findViewById(R.id.keyProfileUpdateBtn);

        keyProfileRecommendField = view.findViewById(R.id.keyProfileRecommend);

        view.findViewById(R.id.config_info_btn).setOnClickListener(v -> {
            sharedUtils.showMessage(requireContext(), "Configuration Version", configInfo);
        });

        view.findViewById(R.id.firmware_info_btn).setOnClickListener(v -> {
            sharedUtils.showMessage(requireContext(), "Firmware Version", firmwareInfo);
        });

        view.findViewById(R.id.key_profile_info_btn).setOnClickListener(v -> {
            sharedUtils.showMessage(requireContext(), "Encryption Key Version", encryptionInfo);
        });

        configUpdateBtn.setOnClickListener(v -> {
            showInfoAlert(DemoOTA.CONFIG_UPDATE);
        });

        firmwareUpdateBtn.setOnClickListener(v -> {
            showInfoAlert(DemoOTA.FIRMWARE_UPDATE);
        });

        keyProfileUpdateBtn.setOnClickListener(v -> {
            showInfoAlert(DemoOTA.KEY_PROFILE_UPDATE);
        });

        try {
            // This calls STEP 5
            mListener.startOTA();
        } catch (BBDeviceControllerNotSupportOTAException | OTAServerURLNotSetException | BBDeviceNotConnectedException | NoInternetConnectionException | BBDeviceControllerNotSetException e) {
            sharedUtils.showMessage(requireContext(), "OTA Error", e.getMessage());
            e.printStackTrace();
        }

        configVersionField.setText(mConfig);
        firmwareVersionField.setText(mFirmware);
        keyProfileVersionField.setText(mKeyProfile);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDemoOTAListener) {
            mListener = (OnDemoOTAListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void showInfoAlert(int updateItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tap Confirm to continue and update");
        builder.setMessage("");
        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            switch (updateItem) {
                case DemoOTA.CONFIG_UPDATE:
                    mListener.doUpdateConfig();
                    break;
                case DemoOTA.FIRMWARE_UPDATE:
                    mListener.doUpdateFirmware();
                    break;
                case DemoOTA.KEY_PROFILE_UPDATE:
                    mListener.doUpdateKeyInjection();
                    break;
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.create().show();
    }

    public void toggleConfigFrame(boolean hide) {
        if (hide) {
            configUpdateFrame.setVisibility(View.GONE);
        } else {
           configUpdateFrame.setVisibility(View.VISIBLE);
        }
    }

    public void toggleFirmwareFrame(boolean hide) {
        if (hide) {
            firmwareUpdateFrame.setVisibility(View.GONE);
        } else {
            firmwareUpdateFrame.setVisibility(View.VISIBLE);
        }
    }

    public void toggleKeyProfileFrame(boolean hide) {
        if (hide) {
            keyProfileUpdateFrame.setVisibility(View.GONE);
            keyProfileRecommendField.setVisibility(View.GONE);
        } else {
            keyProfileUpdateFrame.setVisibility(View.VISIBLE);
            keyProfileRecommendField.setVisibility(View.VISIBLE);
            keyProfileUpdateBtn.setText(R.string.update_title);
        }
    }

    public void updateKeyProfileFrame() {
        keyProfileUpdateFrame.setVisibility(View.VISIBLE);
        keyProfileRecommendField.setVisibility(View.GONE);
        keyProfileUpdateBtn.setText(R.string.force_update);
    }

    public void updateDeviceLatestVersionLabel(String config, String firmware, String keyProfile) {
        payrixConfigVersionField.setText(config);
        payrixFirmwareVersionField.setText(firmware);
        payrixKeyProfileVersionField.setText(keyProfile);
    }
}