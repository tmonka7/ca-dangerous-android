package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falcon.car.R;
import com.falcon.car.data.DtcDecoder;
import com.falcon.car.data.model.Dtc;
import com.falcon.car.obd.ObdManager;
import com.falcon.car.obd.Pid;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Screen 11 - one code in full, plus the freeze frame the ECU stored when the
 * fault was set.
 */
public class DtcDetailFragment extends Fragment {

    private static final String ARG_CODE = "code";
    private static final String ARG_STATUS = "status";

    private static final List<Pid> FREEZE_FRAME_PIDS = Arrays.asList(
            Pid.ENGINE_RPM, Pid.VEHICLE_SPEED, Pid.COOLANT_TEMP,
            Pid.ENGINE_LOAD, Pid.THROTTLE_POSITION, Pid.CONTROL_MODULE_VOLTAGE);

    private LinearLayout freezeFrameCard;
    private View freezeFrameEmpty;

    public static DtcDetailFragment create(String code, Dtc.Status status) {
        DtcDetailFragment fragment = new DtcDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CODE, code);
        args.putString(ARG_STATUS, status.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dtc_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String code = args == null ? "" : args.getString(ARG_CODE, "");
        Dtc.Status status = Dtc.Status.valueOf(
                args == null ? Dtc.Status.STORED.name() : args.getString(ARG_STATUS,
                        Dtc.Status.STORED.name()));

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(code);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        ((TextView) view.findViewById(R.id.detail_code)).setText(code);
        ((TextView) view.findViewById(R.id.detail_description))
                .setText(DtcDecoder.describe(requireContext(), code));
        ((TextView) view.findViewById(R.id.detail_source))
                .setText(DtcDecoder.isKnown(requireContext(), code)
                        ? R.string.dtc_source_dictionary
                        : R.string.dtc_source_decoder);

        bindRow(view.findViewById(R.id.row_system), getString(R.string.label_system),
                getString(DtcDecoder.systemRes(code)));
        bindRow(view.findViewById(R.id.row_kind), getString(R.string.label_type),
                getString(DtcDecoder.isManufacturerSpecific(code)
                        ? R.string.dtc_kind_manufacturer
                        : R.string.dtc_kind_generic));
        bindRow(view.findViewById(R.id.row_status), getString(R.string.label_status),
                getString(status.getLabelRes()));

        View subsystemRow = view.findViewById(R.id.row_subsystem);
        int subsystemRes = DtcDecoder.subsystemRes(code);
        if (subsystemRes == 0) {
            subsystemRow.setVisibility(View.GONE);
        } else {
            bindRow(subsystemRow, getString(R.string.label_subsystem),
                    getString(subsystemRes));
        }

        freezeFrameCard = view.findViewById(R.id.freeze_frame_card);
        freezeFrameEmpty = view.findViewById(R.id.freeze_frame_empty);
        loadFreezeFrame();
    }

    private void bindRow(View row, String label, String value) {
        ((TextView) row.findViewById(R.id.row_label)).setText(label);
        ((TextView) row.findViewById(R.id.row_value)).setText(value);
    }

    /**
     * Mode 02 returns the snapshot stored with the first confirmed fault, so it
     * is frequently empty for pending codes. An empty result is not an error.
     */
    private void loadFreezeFrame() {
        if (!ObdManager.get().isConnected()) {
            showNoFreezeFrame();
            return;
        }
        ObdManager.get().readFreezeFrame(FREEZE_FRAME_PIDS,
                new ObdManager.ResultCallback<Map<Pid, Double>>() {
                    @Override
                    public void onSuccess(Map<Pid, Double> values) {
                        if (!isAdded()) {
                            return;
                        }
                        if (values.isEmpty()) {
                            showNoFreezeFrame();
                        } else {
                            bindFreezeFrame(values);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        if (isAdded()) {
                            showNoFreezeFrame();
                        }
                    }
                });
    }

    private void bindFreezeFrame(Map<Pid, Double> values) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        freezeFrameCard.removeAllViews();
        freezeFrameCard.setVisibility(View.VISIBLE);
        freezeFrameEmpty.setVisibility(View.GONE);

        for (Pid pid : FREEZE_FRAME_PIDS) {
            Double value = values.get(pid);
            if (value == null) {
                continue;
            }
            View row = inflater.inflate(R.layout.item_detail_row, freezeFrameCard, false);
            bindRow(row, PidLabels.labelFor(requireContext(), pid), format(pid, value));
            freezeFrameCard.addView(row);
        }
    }

    private String format(Pid pid, double value) {
        String number = String.format(Locale.getDefault(),
                "%." + pid.getDecimals() + "f", value);
        return number + " " + pid.getUnit();
    }

    private void showNoFreezeFrame() {
        freezeFrameCard.setVisibility(View.GONE);
        freezeFrameEmpty.setVisibility(View.VISIBLE);
    }
}
