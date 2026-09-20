package com.falcon.car.ui;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.AdapterCatalog;
import com.falcon.car.data.AppPrefs;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.DiscoveredDevice;
import com.falcon.car.data.model.Transport;
import com.falcon.car.obd.BluetoothObdConnection;
import com.falcon.car.obd.BluetoothScanner;
import com.falcon.car.obd.DemoObdConnection;
import com.falcon.car.obd.ObdManager;
import com.falcon.car.ui.adapter.DeviceAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen 03 - choose a transport, find an adapter and open the link.
 *
 * <p>Bluetooth Classic is the transport with real I/O; the other tiles are
 * shown because the capability model already covers them, and they say so
 * rather than pretending to work.
 */
public class ConnectDeviceFragment extends Fragment
        implements DeviceAdapter.OnDeviceSelected, ObdManager.StateListener,
        BluetoothScanner.Listener {

    /** Keyed by address so a device found twice does not appear twice. */
    private final Map<String, DiscoveredDevice> devices = new LinkedHashMap<>();

    private DeviceAdapter deviceAdapter;
    private GridLayout transportGrid;
    private TextView deviceCount;
    private TextView connectorValue;
    private MaterialButton searchButton;
    private BluetoothScanner scanner;
    private Transport selectedTransport = Transport.BLUETOOTH_CLASSIC;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect_device, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                granted -> refreshDevices());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scanner = new BluetoothScanner(requireContext(), this);
        selectedTransport = SessionState.get().getTransport();

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_connect_device);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        transportGrid = view.findViewById(R.id.transport_grid);
        deviceCount = view.findViewById(R.id.device_count);
        searchButton = view.findViewById(R.id.btn_search);
        connectorValue = view.findViewById(R.id.connector_value);

        RecyclerView list = view.findViewById(R.id.device_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceAdapter = new DeviceAdapter(this);
        list.setAdapter(deviceAdapter);

        searchButton.setOnClickListener(v -> startScan());
        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> startScan());
        view.findViewById(R.id.btn_catalog).setOnClickListener(
                v -> ((MainActivity) requireActivity()).navigateTo(new DeviceListFragment()));
        view.findViewById(R.id.connector_row).setOnClickListener(
                v -> ((MainActivity) requireActivity())
                        .navigateTo(new ConnectorSelectionFragment()));

        buildTransportTiles();
        refreshDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        connectorValue.setText(SessionState.get().getConnector().getDisplayName());
        ObdManager.get().addListener(this);
        refreshDevices();
    }

    @Override
    public void onPause() {
        ObdManager.get().removeListener(this);
        scanner.stopDiscovery();
        super.onPause();
    }

    private void buildTransportTiles() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing) / 2;

        transportGrid.removeAllViews();
        for (Transport transport : Transport.selectable()) {
            View tile = inflater.inflate(R.layout.item_transport_tile, transportGrid, false);
            UiUtils.setIcon(tile.findViewById(R.id.transport_icon), transport.getIconRes(),
                    R.color.redline_red);
            ((TextView) tile.findViewById(R.id.transport_name)).setText(transport.getLabelRes());
            tile.setSelected(transport == selectedTransport);
            tile.setOnClickListener(v -> selectTransport(transport));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(spacing, spacing, spacing, spacing);
            tile.setLayoutParams(params);

            transportGrid.addView(tile);
        }
    }

    private void selectTransport(Transport transport) {
        if (transport != Transport.BLUETOOTH_CLASSIC) {
            // Honest about the state of the transport layer rather than
            // selecting a tile that cannot open a link.
            Snackbar.make(requireView(),
                    getString(R.string.placeholder_phase_format, 4),
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        selectedTransport = transport;
        SessionState.get().setTransport(transport);

        Transport[] selectable = Transport.selectable();
        for (int i = 0; i < transportGrid.getChildCount() && i < selectable.length; i++) {
            transportGrid.getChildAt(i).setSelected(selectable[i] == transport);
        }
        refreshDevices();
    }

    // ------------------------------------------------------------ discovery

    /** Rebuilds the list from whichever source the current mode implies. */
    private void refreshDevices() {
        if (!isAdded()) {
            return;
        }
        devices.clear();

        if (AppPrefs.isDemoMode(requireContext())) {
            DiscoveredDevice demo = new DiscoveredDevice(
                    new DemoObdConnection().getName(), AdapterCatalog.byBrand("OBDLink"),
                    Transport.BLUETOOTH_CLASSIC, 4, connectionState());
            devices.put("demo", demo);
            publish(getString(R.string.demo_mode_banner));
            return;
        }

        if (!BluetoothScanner.isSupported()) {
            publish(getString(R.string.bluetooth_unsupported));
            return;
        }
        if (!BluetoothScanner.hasPermissions(requireContext())) {
            publish(getString(R.string.bluetooth_permission_needed));
            return;
        }
        if (!BluetoothScanner.isEnabled()) {
            publish(getString(R.string.bluetooth_disabled));
            return;
        }

        for (BluetoothDevice device : scanner.bondedDevices()) {
            addDevice(device, Short.MIN_VALUE);
        }
        publish(devices.isEmpty()
                ? getString(R.string.no_paired_devices)
                : getString(R.string.devices_found_format, devices.size()));
    }

    private void startScan() {
        if (AppPrefs.isDemoMode(requireContext())) {
            refreshDevices();
            return;
        }
        if (!BluetoothScanner.isSupported()) {
            publish(getString(R.string.bluetooth_unsupported));
            return;
        }
        if (!BluetoothScanner.hasPermissions(requireContext())) {
            permissionLauncher.launch(BluetoothScanner.requiredPermissions());
            return;
        }
        if (!BluetoothScanner.isEnabled()) {
            publish(getString(R.string.bluetooth_disabled));
            return;
        }

        refreshDevices();
        searchButton.setEnabled(false);
        deviceCount.setText(R.string.scanning_for_devices);
        scanner.startDiscovery();
    }

    @Override
    public void onDeviceFound(BluetoothDevice device, int rssi) {
        if (!isAdded()) {
            return;
        }
        addDevice(device, rssi);
        publish(getString(R.string.devices_found_format, devices.size()));
    }

    @Override
    public void onScanFinished() {
        if (!isAdded()) {
            return;
        }
        searchButton.setEnabled(true);
        publish(devices.isEmpty()
                ? getString(R.string.no_paired_devices)
                : getString(R.string.devices_found_format, devices.size()));
    }

    private void addDevice(BluetoothDevice device, int rssi) {
        String name = BluetoothScanner.nameOf(device);
        devices.put(device.getAddress(), new DiscoveredDevice(
                name,
                device.getAddress(),
                AdapterCatalog.matchByName(name),
                Transport.BLUETOOTH_CLASSIC,
                BluetoothScanner.signalLevel(rssi),
                stateFor(device.getAddress())));
    }

    private DiscoveredDevice.State stateFor(String address) {
        DiscoveredDevice connected = SessionState.get().getConnectedDevice();
        if (connected != null && address.equals(connected.getAddress())
                && ObdManager.get().isConnected()) {
            return DiscoveredDevice.State.CONNECTED;
        }
        return DiscoveredDevice.State.AVAILABLE;
    }

    private DiscoveredDevice.State connectionState() {
        return ObdManager.get().isConnected()
                ? DiscoveredDevice.State.CONNECTED
                : DiscoveredDevice.State.AVAILABLE;
    }

    private void publish(String status) {
        deviceAdapter.submit(new ArrayList<>(devices.values()));
        deviceCount.setText(status);
    }

    // ----------------------------------------------------------- connecting

    @Override
    public void onDeviceSelected(DiscoveredDevice device) {
        scanner.stopDiscovery();
        SessionState.get().setConnectedDevice(device);

        if (device.isSimulated()) {
            ObdManager.get().connect(new DemoObdConnection());
            return;
        }
        if (!BluetoothScanner.hasPermissions(requireContext())) {
            permissionLauncher.launch(BluetoothScanner.requiredPermissions());
            return;
        }
        BluetoothDevice target = BluetoothScanner.adapter()
                .getRemoteDevice(device.getAddress());
        ObdManager.get().connect(new BluetoothObdConnection(target, device.getName()));
    }

    @Override
    public void onObdState(ObdManager.State state, String detail) {
        if (!isAdded()) {
            return;
        }
        switch (state) {
            case CONNECTING:
                deviceCount.setText(getString(R.string.connecting_to_format, detail));
                searchButton.setEnabled(false);
                break;
            case CONNECTED:
                searchButton.setEnabled(true);
                refreshDevices();
                deviceCount.setText(detail.isEmpty()
                        ? getString(R.string.status_connected)
                        : getString(R.string.protocol_format, detail));
                Snackbar.make(requireView(),
                        getString(R.string.connected_to_format,
                                ObdManager.get().getAdapterName()),
                        Snackbar.LENGTH_SHORT).show();
                break;
            case FAILED:
                searchButton.setEnabled(true);
                SessionState.get().setConnectedDevice(null);
                refreshDevices();
                Snackbar.make(requireView(),
                        getString(R.string.connect_failed_format, detail),
                        Snackbar.LENGTH_LONG).show();
                break;
            default:
                refreshDevices();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        scanner.stopDiscovery();
        super.onDestroyView();
    }
}
