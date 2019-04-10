import android.bluetooth.*;
import android.util.Log;
import java.util.Set;
import android.app.Activity;
import android.content.Context;


public class BleConnector {
    private final static String TAG = "BleUnity";
    private int connectionState = STATE_DISCONNECTED;
    private Context appContext;

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
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

    };

    private BleConnector() {

    }

    public void setContext(Context unityContext) {
        appContext = unityContext;
    }


    private BluetoothAdapter adapter;
    BluetoothDevice activeDevice;
    Set<BluetoothDevice> devices;
    BluetoothGatt bluetoothGatt;

    public void BleConnect(String deviceName) {

        adapter = BluetoothAdapter.getDefaultAdapter();
        devices = adapter.getBondedDevices();
        for(BluetoothDevice device:devices)
        {
            Log.i(TAG, "Found Device: " + device.getName());
            if (device.getName().equals(deviceName)) {
                activeDevice = adapter.getRemoteDevice(device.getAddress());
                bluetoothGatt = device.connectGatt(appContext, false, gattCallback);
            };
        }
    }
}
