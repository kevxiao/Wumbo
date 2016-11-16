package com.star.patrick.wumbo.wifidirect;


import android.app.Activity;
import android.util.Log;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.util.Random;

import static com.star.patrick.wumbo.MainActivity.TAG;

public class SalutManager {
    public SalutManager(Activity activity) {
        SalutDataReceiver dataReceiver = new SalutDataReceiver(activity, new SalutDataCallback() {
            @Override
            public void onDataReceived(Object o) {

            }
        });
        SalutServiceData serviceData = new SalutServiceData("wumbo", MessageDispatcherService.PORT, "User " + new Random().nextInt());

        Salut network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

        network.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                Log.d(TAG, device.readableName + " has connected!");
            }
        });

        network.discoverNetworkServices(new SalutCallback() {
            @Override
            public void call() {
                Log.d(TAG, "All I know is that a device has connected.");
            }
        }, true);
    }



}
