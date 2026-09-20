package com.falcon.car.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.MockData;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.DiscoveredDevice;
import com.falcon.car.data.model.Transport;
import com.falcon.car.ui.adapter.DeviceAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** Screen 03 - pick a transport, scan, and connect to a VCI. */
public class ConnectDeviceFragment extends Fragment implements DeviceAdapter.OnDeviceSelected {

    private static final long SCAN_MS = 1400L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<DiscoveredDevice> allDevices = new ArrayList<>();

    private DeviceAdapter deviceAdapter;
    private GridLayout transportGrid;
    private TextView deviceCount;
    private TextView connectorValue;
    private MaterialButton searchButton;
    private Transport selectedTransport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allDevices.clear();
        allDevices.addAll(MockData.discoveredDevices());
        selectedTransport = SessionState.get().getTransport();

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_connect_device);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        transportGrid = view.findViewById(R.id.transport_grid);
        deviceCount = view.findViewById(R.id.device_count);
        searchButton = view.findViewById(R.id.btn_search);

        RecyclerView list = view.findViewById(R.id.device_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceAdapter = new DeviceAdapter(this);
        list.setAdapter(deviceAdapter);

        searchButton.setOnClickListener(v -> startScan());
        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> startScan());
        view.findViewById(R.id.btn_catalog).setOnClickListener(
                v -> ((MainActivity) requireActivity()).navigateTo(new DeviceListFragment()));

        View connectorRow = view.findViewById(R.id.connector_row);
        connectorValue = view.findViewById(R.id.connector_value);
        connectorRow.setOnClickListener(
                v -> ((MainActivity) requireActivity()).navigateTo(new ConnectorSelectionFragment()));

        buildTransportTiles();
        showDevices();
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
        selectedTransport = transport;
        SessionState.get().setTransport(transport);

        Transport[] selectable = Transport.selectable();
        for (int i = 0; i < transportGrid.getChildCount() && i < selectable.length; i++) {
            transportGrid.getChildAt(i).setSelected(selectable[i] == transport);
        }
        showDevices();
    }

    /** Fakes a discovery sweep; the real scan replaces this in phase 4. */
    private void startScan() {
        handler.removeCallbacksAndMessages(null);
        deviceAdapter.submit(new ArrayList<>());
        deviceCount.setText(R.string.scanning_for_devices);
        searchButton.setEnabled(false);

        handler.postDelayed(() -> {
            searchButton.setEnabled(true);
            showDevices();
        }, SCAN_MS);
    }

    private void showDevices() {
        List<DiscoveredDevice> visible = new ArrayList<>();
        for (DiscoveredDevice device : allDevices) {
            if (device.getProfile() != null
                    && device.getProfile().getTransports().contains(selectedTransport)) {
                visible.add(device);
            }
        }
        deviceAdapter.submit(visible);
        deviceCount.setText(getString(R.string.devices_found_format, visible.size()));
    }

    @Override
    public void onDeviceSelected(DiscoveredDevice device) {
        for (DiscoveredDevice other : allDevices) {
            other.setState(DiscoveredDevice.State.AVAILABLE);
        }
        device.setState(DiscoveredDevice.State.CONNECTED);
        SessionState.get().setConnectedDevice(device);

        showDevices();
        Snackbar.make(requireView(),
                getString(R.string.connected_to_format, device.getName()),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        connectorValue.setText(SessionState.get().getConnector().getDisplayName());
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
