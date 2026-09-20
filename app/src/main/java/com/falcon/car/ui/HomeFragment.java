package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.Monitor;
import com.falcon.car.data.model.Vehicle;
import com.falcon.car.obd.MonitorStatus;
import com.falcon.car.obd.ObdManager;
import com.falcon.car.obd.Pid;
import com.falcon.car.ui.widget.GaugeView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Screen 02 - the dashboard. Everything here comes from the live link: speed
 * and rpm from mode 01, and the lamp, fault count and readiness monitors from
 * PID 01. With no adapter connected the values read as dashes rather than as
 * plausible-looking numbers.
 */
public class HomeFragment extends Fragment implements ObdManager.StateListener {

    private static final long POLL_INTERVAL_MS = 900L;
    private static final String NO_VALUE = "\u2013";

    private static final List<Pid> LIVE_PIDS = Arrays.asList(
            Pid.VEHICLE_SPEED, Pid.ENGINE_RPM, Pid.COOLANT_TEMP,
            Pid.CONTROL_MODULE_VOLTAGE);

    private TextView vehicleName;
    private TextView vehicleSpec;
    private TextView connectionLabel;
    private View connectionDot;
    private GaugeView speedGauge;
    private TextView rpmReadout;
    private GridLayout monitorsGrid;
    private TextView healthScore;
    private TextView milStatus;
    private LinearProgressIndicator healthBar;
    private MaterialButton scanButton;
    private TextView scanHint;

    private View statDtc;
    private View statBattery;
    private View statCoolant;

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
        monitorsGrid = view.findViewById(R.id.systems_grid);
        healthScore = view.findViewById(R.id.health_score);
        healthBar = view.findViewById(R.id.health_bar);
        milStatus = view.findViewById(R.id.mil_status);
        scanButton = view.findViewById(R.id.btn_full_scan);
        scanHint = view.findViewById(R.id.scan_hint);

        statDtc = view.findViewById(R.id.stat_dtc);
        statBattery = view.findViewById(R.id.stat_battery);
        statCoolant = view.findViewById(R.id.stat_coolant);

        view.findViewById(R.id.connection_chip).setOnClickListener(
                v -> host().navigateTo(new ConnectDeviceFragment()));
        view.findViewById(R.id.vehicle_card).setOnClickListener(
                v -> host().navigateTo(VehicleSelectionFragment.asDetail()));
        scanButton.setOnClickListener(v -> startScan());

        speedGauge.setMax(240f);
        speedGauge.setUnit(getString(R.string.unit_kmh));
        speedGauge.setLabel(getString(R.string.state_ready));

        showDisconnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindVehicle();
        ObdManager.get().addListener(this);
        onObdState(ObdManager.get().getState(), ObdManager.get().getDetail());
    }

    @Override
    public void onPause() {
        ObdManager.get().removeListener(this);
        ObdManager.get().stopPolling();
        super.onPause();
    }

    @Override
    public void onObdState(ObdManager.State state, String detail) {
        if (!isAdded()) {
            return;
        }
        boolean connected = state == ObdManager.State.CONNECTED;
        boolean connecting = state == ObdManager.State.CONNECTING;

        int labelRes = R.string.status_disconnected;
        if (connected) {
            labelRes = R.string.status_connected;
        } else if (connecting) {
            labelRes = R.string.status_connecting;
        }
        connectionLabel.setText(labelRes);
        UiUtils.tintBackground(connectionDot,
                connected ? R.color.state_good : R.color.text_tertiary);
        scanHint.setVisibility(connected ? View.GONE : View.VISIBLE);

        if (connected) {
            startLiveData();
        } else {
            ObdManager.get().stopPolling();
            showDisconnected();
        }
    }

    private void bindVehicle() {
        Vehicle vehicle = SessionState.get().getVehicle();
        vehicleName.setText(vehicle.getDisplayName());
        vehicleSpec.setText(vehicle.getSpecLine());
    }

    // ------------------------------------------------------------ live data

    private void startLiveData() {
        ObdManager.get().readStatus(new ObdManager.ResultCallback<MonitorStatus>() {
            @Override
            public void onSuccess(MonitorStatus status) {
                if (isAdded()) {
                    bindStatus(status);
                }
            }

            @Override
            public void onFailure(String message) {
                if (isAdded()) {
                    showDisconnected();
                }
            }
        });

        ObdManager.get().startPolling(LIVE_PIDS, POLL_INTERVAL_MS,
                new ObdManager.ResultCallback<Map<Pid, Double>>() {
                    @Override
                    public void onSuccess(Map<Pid, Double> values) {
                        if (isAdded()) {
                            bindLive(values);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        // A single missed sweep on a busy bus is not worth reporting.
                    }
                });
    }

    private void bindLive(Map<Pid, Double> values) {
        Double speed = values.get(Pid.VEHICLE_SPEED);
        speedGauge.animateTo(speed == null ? 0f : speed.floatValue());

        Double rpm = values.get(Pid.ENGINE_RPM);
        rpmReadout.setText(rpm == null
                ? getString(R.string.label_rpm) + " " + NO_VALUE
                : getString(R.string.label_rpm) + " " + Math.round(rpm));

        Double voltage = values.get(Pid.CONTROL_MODULE_VOLTAGE);
        bindStat(statBattery, R.drawable.ic_battery,
                voltage == null ? NO_VALUE
                        : String.format(Locale.getDefault(), "%.1f %s", voltage,
                                getString(R.string.unit_volt)),
                getString(R.string.label_battery), R.color.text_secondary);

        Double coolant = values.get(Pid.COOLANT_TEMP);
        bindStat(statCoolant, R.drawable.ic_coolant,
                coolant == null ? NO_VALUE
                        : Math.round(coolant) + getString(R.string.unit_celsius),
                getString(R.string.label_coolant), R.color.text_secondary);
    }

    // -------------------------------------------------------------- status

    private void bindStatus(MonitorStatus status) {
        int faults = status.getDtcCount();

        bindStat(statDtc, R.drawable.ic_dtc, String.valueOf(faults),
                getString(R.string.label_dtc),
                faults > 0 ? R.color.state_fault : R.color.state_good);

        int score = healthScoreFor(status);
        healthScore.setText(String.format(Locale.getDefault(), "%d%%", score));
        healthBar.setProgressCompat(score, true);

        milStatus.setText(getString(status.isMilOn() ? R.string.mil_on : R.string.mil_off)
                + " \u00B7 "
                + getString(R.string.monitors_format,
                        status.supportedCount() - status.incompleteCount(),
                        status.supportedCount()));
        milStatus.setTextColor(ContextCompat.getColor(requireContext(),
                status.isMilOn() ? R.color.state_fault : R.color.state_good));

        bindMonitors(status);
    }

    /**
     * A summary the app computes, not a value the vehicle reports: the lamp
     * costs 20 points, each confirmed fault 8, and each monitor that has not
     * finished its drive cycle 3.
     */
    private int healthScoreFor(MonitorStatus status) {
        int score = 100;
        if (status.isMilOn()) {
            score -= 20;
        }
        score -= status.getDtcCount() * 8;
        score -= status.incompleteCount() * 3;
        return Math.max(0, Math.min(100, score));
    }

    private void bindMonitors(MonitorStatus status) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing) / 2;

        monitorsGrid.removeAllViews();
        for (Monitor monitor : Monitor.values()) {
            if (!status.isSupported(monitor)) {
                continue; // The ECU does not run this monitor at all.
            }
            boolean ready = status.isComplete(monitor);
            View chip = inflater.inflate(R.layout.item_system_chip, monitorsGrid, false);

            UiUtils.setIcon(chip.findViewById(R.id.system_icon), monitor.getIconRes(),
                    R.color.text_secondary);
            UiUtils.setIcon(chip.findViewById(R.id.system_status_icon),
                    ready ? R.drawable.ic_check : R.drawable.ic_pending,
                    ready ? R.color.state_good : R.color.state_warning);

            ((TextView) chip.findViewById(R.id.system_name)).setText(monitor.getLabelRes());

            TextView label = chip.findViewById(R.id.system_status_label);
            label.setText(ready ? R.string.monitor_ready : R.string.monitor_not_ready);
            label.setTextColor(ContextCompat.getColor(requireContext(),
                    ready ? R.color.state_good : R.color.state_warning));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(spacing, spacing, spacing, spacing);
            chip.setLayoutParams(params);

            monitorsGrid.addView(chip);
        }
    }

    /** Nothing is known without a link, so the readouts say so. */
    private void showDisconnected() {
        speedGauge.setValue(0f);
        rpmReadout.setText(getString(R.string.label_rpm) + " " + NO_VALUE);
        monitorsGrid.removeAllViews();
        healthScore.setText(NO_VALUE);
        healthBar.setProgressCompat(0, false);
        milStatus.setText(R.string.not_connected_hint);
        milStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));

        bindStat(statDtc, R.drawable.ic_dtc, NO_VALUE, getString(R.string.label_dtc),
                R.color.text_tertiary);
        bindStat(statBattery, R.drawable.ic_battery, NO_VALUE,
                getString(R.string.label_battery), R.color.text_tertiary);
        bindStat(statCoolant, R.drawable.ic_coolant, NO_VALUE,
                getString(R.string.label_coolant), R.color.text_tertiary);
    }

    private void bindStat(View stat, int iconRes, String value, String label, int iconColorRes) {
        UiUtils.setIcon(stat.findViewById(R.id.stat_icon), iconRes, iconColorRes);
        ((TextView) stat.findViewById(R.id.stat_value)).setText(value);
        ((TextView) stat.findViewById(R.id.stat_label)).setText(label);
    }

    /** Without a link there is nothing to scan, so connect first. */
    private void startScan() {
        if (ObdManager.get().isConnected()) {
            host().selectTab(R.id.nav_diagnose);
        } else {
            host().navigateTo(new ConnectDeviceFragment());
        }
    }

    private MainActivity host() {
        return (MainActivity) requireActivity();
    }
}
