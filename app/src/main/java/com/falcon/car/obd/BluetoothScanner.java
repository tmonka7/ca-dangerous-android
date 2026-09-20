package com.falcon.car.obd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Bonded-device listing and inquiry scanning for Bluetooth Classic adapters.
 *
 * <p>Most OBD dongles have to be paired in Android settings before they answer,
 * so the bonded list is the primary source and discovery is there to surface
 * adapters that are powered but not yet paired.
 */
@SuppressLint("MissingPermission")
public class BluetoothScanner {

    public interface Listener {
        void onDeviceFound(BluetoothDevice device, int rssi);

        void onScanFinished();
    }

    private final Context context;
    private final Listener listener;
    private BroadcastReceiver receiver;

    public BluetoothScanner(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public static BluetoothAdapter adapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static boolean isSupported() {
        return adapter() != null;
    }

    public static boolean isEnabled() {
        BluetoothAdapter adapter = adapter();
        return adapter != null && adapter.isEnabled();
    }

    /** Permissions this Android version needs before scanning can start. */
    public static String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }
        // Before API 31 an inquiry scan counts as a location request.
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }

    public static boolean hasPermissions(Context context) {
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Devices already paired with this phone. */
    public List<BluetoothDevice> bondedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        BluetoothAdapter adapter = adapter();
        if (adapter == null || !hasPermissions(context)) {
            return devices;
        }
        try {
            devices.addAll(adapter.getBondedDevices());
        } catch (SecurityException e) {
            return devices;
        }
        return devices;
    }

    /** Starts an inquiry scan. Safe to call repeatedly; restarts the scan. */
    public void startDiscovery() {
        BluetoothAdapter adapter = adapter();
        if (adapter == null || !adapter.isEnabled() || !hasPermissions(context)) {
            listener.onScanFinished();
            return;
        }

        stopDiscovery();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
                            Short.MIN_VALUE);
                    if (device != null) {
                        listener.onDeviceFound(device, rssi);
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    listener.onScanFinished();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);

        try {
            adapter.startDiscovery();
        } catch (SecurityException e) {
            listener.onScanFinished();
        }
    }

    public void stopDiscovery() {
        BluetoothAdapter adapter = adapter();
        if (adapter != null) {
            try {
                adapter.cancelDiscovery();
            } catch (SecurityException ignored) {
                // Permission was revoked mid-scan; nothing to cancel.
            }
        }
        if (receiver != null) {
            try {
                context.unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {
                // Already unregistered.
            }
            receiver = null;
        }
    }

    /** Maps RSSI in dBm onto the four bars the list draws. */
    public static int signalLevel(int rssi) {
        if (rssi == Short.MIN_VALUE) {
            return 3;
        }
        if (rssi >= -60) {
            return 4;
        }
        if (rssi >= -70) {
            return 3;
        }
        if (rssi >= -80) {
            return 2;
        }
        return 1;
    }

    public static String nameOf(BluetoothDevice device) {
        try {
            String name = device.getName();
            return name == null || name.isEmpty() ? device.getAddress() : name;
        } catch (SecurityException e) {
            return device.getAddress();
        }
    }
}
