package com.star.patrick.wumbo.wifidirect;


import android.app.Activity;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;
import com.star.patrick.wumbo.Message;

import java.io.IOException;
import java.util.Random;

import static com.star.patrick.wumbo.MainActivity.TAG;

public class SalutManager implements AutoCloseable {

    private Salut network;

    public SalutManager(Activity activity) {
        SalutDataReceiver dataReceiver = new SalutDataReceiver(activity, new DataReceiver());
        SalutServiceData serviceData = new SalutServiceData("wumbo", MessageDispatcherService.PORT, "User " + new Random().nextInt());

        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

        network.startNetworkService(new DeviceConnected());

//        network.discoverNetworkServices(new SalutDeviceCallback() {
//            @Override
//            public void call(SalutDevice device) {
//                Log.d(TAG, "A device has connected with the name " + device.deviceName);
//                network.registerWithHost(device, new SalutCallback() {
//                    @Override
//                    public void call() {
//                        Log.d(TAG, "We're now registered.");
//                    }
//                }, new SalutCallback() {
//                    @Override
//                    public void call() {
//                        Log.d(TAG, "We failed to register.");
//                    }
//                });
//            }
//        }, true);
    }

    @Override
    public void close() {
        network.stopNetworkService(false);
        network.unregisterClient(false);
    }

    public void send(Message msg) {
        network.sendToAllDevices(msg, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Oh no! The data failed to send.");
            }
        });
    }

    private class DataReceiver implements SalutDataCallback {
        @Override
        public void onDataReceived(Object data) {
            Log.d(TAG, "Received network data.");
            try {
                Message newMessage = LoganSquare.parse((String) data, Message.class);
                Log.d(TAG, newMessage.getText());  //See you on the other side!
                //Do other stuff with data.
            } catch (IOException ex) {
                Log.e(TAG, "Failed to parse network data.");
            }
        }
    }

    private class DeviceConnected implements SalutDeviceCallback {
        @Override
        public void call(SalutDevice salutDevice) {
            Log.d(TAG, "Device registered with host.");
        }
    }

}
