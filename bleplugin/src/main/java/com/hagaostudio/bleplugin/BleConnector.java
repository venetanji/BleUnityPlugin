package com.hagaostudio.bleplugin;

import android.bluetooth.*;
import android.util.Log;
import java.util.Set;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.content.Context;
import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;


public class BleConnector {
    private final static String TAG = "BleUnity";
    public static final String SerialProtUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
    private int connectionState = STATE_DISCONNECTED;
    private Context appContext;
    private Activity appActivity;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.hagaostudio.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.hagaostudio.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.hagaostudio.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.hagaostudio.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.hagaostudio.bluetooth.le.EXTRA_DATA";


    private static final BleConnector ourInstance = new BleConnector();

    public static BleConnector getInstance() {
        return ourInstance;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "State: " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService gattService : gatt.getServices()) {
                    for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        Log.i(TAG, "Characteristic discovered:  " + gattCharacteristic.getUuid().toString());
                    }
                    Log.i(TAG, "onServicesDiscovered received: " + gattService.getUuid().toString());
                }

            }

        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "onServicesDiscovered received: " + status);
        }

    };

    private BleConnector() {


    }

    public void setContext(Context unityContext) {
        appContext = unityContext;
    }
    public void setActivity(Activity unityActivity) { appActivity = unityActivity; }


    private BluetoothAdapter adapter;
    BluetoothDevice activeDevice;
    Set<BluetoothDevice> devices;
    BluetoothGatt bluetoothGatt;

    public void BleConnect(String deviceName) {
        if (appActivity.checkSelfPermission(Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No Permissions, asking ");
            appActivity.requestPermissions( new String[]{Manifest.permission.BLUETOOTH}, 0);
        }
        adapter = BluetoothAdapter.getDefaultAdapter();
        devices = adapter.getBondedDevices();
        for(BluetoothDevice device:devices)
        {
            Log.i(TAG, "Found Device: " + device.getName());
            if (device.getName().equals(deviceName)) {
                Log.i(TAG, "Found device ");
                activeDevice = adapter.getRemoteDevice(device.getAddress());
                bluetoothGatt = device.connectGatt(appContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            };
        }
    }
}
