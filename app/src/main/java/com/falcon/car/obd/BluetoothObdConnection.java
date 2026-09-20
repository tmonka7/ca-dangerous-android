package com.falcon.car.obd;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * RFCOMM link to a Bluetooth Classic ELM327-style adapter.
 *
 * <p>Callers are responsible for holding BLUETOOTH_CONNECT (API 31+) before
 * constructing this; the permission checks live in the UI layer where they can
 * prompt.
 */
@SuppressLint("MissingPermission")
public class BluetoothObdConnection implements ObdConnection {

    /** Serial Port Profile - what every ELM327 clone exposes. */
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final char PROMPT = '>';

    private final BluetoothDevice device;
    private final String name;

    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;

    public BluetoothObdConnection(BluetoothDevice device, String name) {
        this.device = device;
        this.name = name;
    }

    @Override
    public void open() throws IOException {
        // Discovery and a connect attempt fight over the radio.
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
        } catch (IOException primary) {
            // Several clones only answer on the undocumented channel-1 route.
            closeQuietly();
            socket = createFallbackSocket();
            if (socket == null) {
                throw primary;
            }
            socket.connect();
        }

        input = socket.getInputStream();
        output = socket.getOutputStream();
    }

    private BluetoothSocket createFallbackSocket() {
        try {
            return (BluetoothSocket) device.getClass()
                    .getMethod("createRfcommSocket", int.class)
                    .invoke(device, 1);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        closeQuietly();
        input = null;
        output = null;
    }

    private void closeQuietly() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Nothing useful to do while tearing down.
            }
            socket = null;
        }
    }

    @Override
    public boolean isOpen() {
        return socket != null && socket.isConnected();
    }

    @Override
    public String send(String command, long timeoutMs) throws IOException {
        if (output == null || input == null) {
            throw new ObdException("Not connected");
        }

        output.write((command + "\r").getBytes("US-ASCII"));
        output.flush();

        return readUntilPrompt(timeoutMs);
    }

    /**
     * ELM327 answers are terminated by a '>' prompt rather than a newline, so
     * the read runs until the prompt arrives or the deadline passes.
     */
    private String readUntilPrompt(long timeoutMs) throws IOException {
        StringBuilder builder = new StringBuilder();
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                int value = input.read();
                if (value == -1) {
                    throw new ObdException("Adapter closed the link");
                }
                char c = (char) value;
                if (c == PROMPT) {
                    return builder.toString();
                }
                builder.append(c);
            } else {
                try {
                    Thread.sleep(8L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ObdException("Interrupted while reading");
                }
            }
        }
        throw new ObdException("Adapter did not answer in time");
    }

    @Override
    public String getName() {
        return name;
    }
}
