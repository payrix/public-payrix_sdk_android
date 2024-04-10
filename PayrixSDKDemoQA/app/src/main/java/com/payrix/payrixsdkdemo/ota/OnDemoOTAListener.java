package com.payrix.payrixsdkdemo.ota;

import com.bbpos.bbdevice.ota.BBDeviceControllerNotSetException;
import com.bbpos.bbdevice.ota.BBDeviceControllerNotSupportOTAException;
import com.bbpos.bbdevice.ota.BBDeviceNotConnectedException;
import com.bbpos.bbdevice.ota.NoInternetConnectionException;
import com.bbpos.bbdevice.ota.OTAServerURLNotSetException;

import java.util.Hashtable;

public interface OnDemoOTAListener {
    void startOTA() throws BBDeviceControllerNotSupportOTAException, OTAServerURLNotSetException, BBDeviceNotConnectedException, NoInternetConnectionException, BBDeviceControllerNotSetException;
    void gotoDeviceUpdate(Hashtable<String, String> deviceInfo);
    void doUpdateConfig();
    void doUpdateFirmware();
    void doUpdateKeyInjection();
}
