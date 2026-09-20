package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.MockData;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.Vehicle;
import com.falcon.car.data.model.VehicleSystem;
import com.falcon.car.ui.widget.GaugeView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

/** Screen 02 - the dashboard: vehicle, link state, telemetry and health. */
public class HomeFragment extends Fragment {

    private TextView vehicleName;
    private TextView vehicleSpec;
    private TextView connectionLabel;
    private View connectionDot;
    private GaugeView speedGauge;
    private TextView rpmReadout;
    private GridLayout systemsGrid;
    private TextView healthScore;
    private LinearProgressIndicator healthBar;
    private MaterialButton scanButton;
    private TextView scanHint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vehicleName = view.findViewById(R.id.vehicle_name);
        vehicleSpec = view.findViewById(R.id.vehicle_spec);
        connectionLabel = view.findViewById(R.id.connection_label);
        connectionDot = view.findViewById(R.id.connection_dot);
        speedGauge = view.findViewById(R.id.speed_gauge);
        rpmReadout = view.findViewById(R.id.rpm_readout);
        systemsGrid = view.findViewById(R.id.systems_grid);
        healthScore = view.findViewById(R.id.health_score);
        healthBar = view.findViewById(R.id.health_bar);
        scanButton = view.findViewById(R.id.btn_full_scan);
        scanHint = view.findViewById(R.id.scan_hint);

        view.findViewById(R.id.connection_chip).setOnClickListener(
                v -> host().navigateTo(new ConnectDeviceFragment()));
        view.findViewById(R.id.vehicle_card).setOnClickListener(
                v -> host().navigateTo(VehicleSelectionFragment.asDetail()));
        scanButton.setOnClickListener(v -> startScan());

        bindTelemetry();
        bindSystems();
        bindHealth(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // The vehicle and the link can both change on other screens.
        bindVehicle();
        bindConnection();
    }

    private void bindVehicle() {
        Vehicle vehicle = SessionState.get().getVehicle();
        vehicleName.setText(vehicle.getDisplayName());
        vehicleSpec.setText(vehicle.getSpecLine());
    }

    private void bindConnection() {
        boolean connected = SessionState.get().isConnected();
        connectionLabel.setText(connected ? R.string.status_connected : R.string.status_disconnected);
        UiUtils.tintBackground(connectionDot,
                connected ? R.color.state_good : R.color.text_tertiary);
        scanHint.setVisibility(connected ? View.GONE : View.VISIBLE);
    }

    private void bindTelemetry() {
        speedGauge.setMax(240f);
        speedGauge.setValue(0f);
        speedGauge.setUnit(getString(R.string.unit_kmh));
        speedGauge.setLabel(getString(R.string.state_ready));
        rpmReadout.setText(String.format(Locale.getDefault(), "%s %d",
                getString(R.string.label_rpm), MockData.engineRpm()));
    }

    private void bindSystems() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<VehicleSystem> systems = MockData.systems();
        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing) / 2;

        systemsGrid.removeAllViews();
        for (VehicleSystem system : systems) {
            View chip = inflater.inflate(R.layout.item_system_chip, systemsGrid, false);
            bindSystemChip(chip, system);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(spacing, spacing, spacing, spacing);
            chip.setLayoutParams(params);

            systemsGrid.addView(chip);
        }
    }

    private void bindSystemChip(View chip, VehicleSystem system) {
        VehicleSystem.Status status = system.getStatus();

        UiUtils.setIcon(chip.findViewById(R.id.system_icon),
                system.getId().getIconRes(), R.color.text_secondary);
        UiUtils.setIcon(chip.findViewById(R.id.system_status_icon),
                status.getIconRes(), status.getColorRes());

        ((TextView) chip.findViewById(R.id.system_name)).setText(system.getId().getLabelRes());

        TextView statusLabel = chip.findViewById(R.id.system_status_label);
        statusLabel.setText(status.getLabelRes());
        statusLabel.setTextColor(ContextCompat.getColor(requireContext(), status.getColorRes()));
    }

    private void bindHealth(View root) {
        int score = MockData.healthScore();
        healthScore.setText(String.format(Locale.getDefault(), "%d%%", score));
        healthBar.setProgressCompat(score, true);

        bindStat(root.findViewById(R.id.stat_dtc), R.drawable.ic_dtc,
                String.valueOf(MockData.dtcCount()), getString(R.string.label_dtc),
                MockData.dtcCount() > 0 ? R.color.state_fault : R.color.state_good);

        bindStat(root.findViewById(R.id.stat_battery), R.drawable.ic_battery,
                String.format(Locale.getDefault(), "%.1f %s", MockData.batteryVoltage(),
                        getString(R.string.unit_volt)),
                getString(R.string.label_battery), R.color.text_secondary);

        bindStat(root.findViewById(R.id.stat_coolant), R.drawable.ic_coolant,
                String.format(Locale.getDefault(), "%d%s", MockData.coolantTemp(),
                        getString(R.string.unit_celsius)),
                getString(R.string.label_coolant), R.color.text_secondary);
    }

    private void bindStat(View stat, int iconRes, String value, String label, int iconColorRes) {
        UiUtils.setIcon(stat.findViewById(R.id.stat_icon), iconRes, iconColorRes);
        ((TextView) stat.findViewById(R.id.stat_value)).setText(value);
        ((TextView) stat.findViewById(R.id.stat_label)).setText(label);
    }

    /** Without a link there is nothing to scan, so send the user to connect first. */
    private void startScan() {
        if (SessionState.get().isConnected()) {
            host().selectTab(R.id.nav_diagnose);
        } else {
            host().navigateTo(new ConnectDeviceFragment());
        }
    }

    private MainActivity host() {
        return (MainActivity) requireActivity();
    }
}
