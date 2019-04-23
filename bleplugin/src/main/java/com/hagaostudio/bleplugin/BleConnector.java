package com.hagaostudio.bleplugin;

import android.bluetooth.*;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.content.Context;
import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Xml;


public class BleConnector {
    private final static String TAG = "BleUnity";
    public static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic serialChar;
    private BluetoothGattCharacteristic commandChar;

    private int connectionState = STATE_DISCONNECTED;
    private Context appContext;
    private Activity appActivity;
    public String incomingData;

    private int mBaudrate=115200;	//set the default baud rate to 115200
    private String mPassword="AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


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
                        if (gattCharacteristic.getUuid().toString().equals(SerialPortUUID)) {
                            Log.i(TAG, "SerialPort char found:  " + gattCharacteristic.getUuid().toString());
                            serialChar = gattCharacteristic;
                        }

                        if (gattCharacteristic.getUuid().toString().equals(CommandUUID)) {
                            Log.i(TAG, "SerialPort command found:  " + gattCharacteristic.getUuid().toString());
                            commandChar = gattCharacteristic;
                        }
                    }
                    Log.i(TAG, "onServicesDiscovered received: " + gattService.getUuid().toString());
                }

            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            incomingData = new String(characteristic.getValue());
            Log.i(TAG,"onCharacteristicChanged  "+incomingData);

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
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No Permissions, asking ");
            ActivityCompat.requestPermissions( appActivity, new String[]{Manifest.permission.BLUETOOTH}, 0);
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

        bluetoothGatt.setCharacteristicNotification(serialChar, true);
    }

}
