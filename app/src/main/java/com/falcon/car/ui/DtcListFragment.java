package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.AppPrefs;
import com.falcon.car.data.model.Dtc;
import com.falcon.car.obd.ObdManager;
import com.falcon.car.ui.adapter.DtcAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 10 - stored, pending and permanent codes read from the vehicle, each
 * resolved through the bundled dictionary.
 */
public class DtcListFragment extends Fragment implements DtcAdapter.OnDtcSelected {

    private DtcAdapter adapter;
    private TextView summary;
    private TextView breakdown;
    private TextView empty;
    private MaterialButton readButton;
    private MaterialButton clearButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dtc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_dtc);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);

        summary = view.findViewById(R.id.dtc_summary);
        breakdown = view.findViewById(R.id.dtc_breakdown);
        empty = view.findViewById(R.id.dtc_empty);
        readButton = view.findViewById(R.id.btn_read_codes);
        clearButton = view.findViewById(R.id.btn_clear_codes);

        view.findViewById(R.id.demo_banner).setVisibility(
                AppPrefs.isDemoMode(requireContext()) ? View.VISIBLE : View.GONE);

        RecyclerView list = view.findViewById(R.id.dtc_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DtcAdapter(this);
        list.setAdapter(adapter);

        readButton.setOnClickListener(v -> readCodes());
        clearButton.setOnClickListener(v -> confirmClear());

        showEmpty(R.string.dtc_reading);
        if (ObdManager.get().isConnected()) {
            readCodes();
        } else {
            showNotConnected();
        }
    }

    private void showNotConnected() {
        summary.setText(R.string.status_disconnected);
        breakdown.setText(R.string.not_connected_hint);
        adapter.submit(new ArrayList<>());
        showEmpty(R.string.not_connected_hint);
        readButton.setText(R.string.title_connect_device);
        readButton.setOnClickListener(
                v -> ((MainActivity) requireActivity()).navigateTo(new ConnectDeviceFragment()));
        clearButton.setEnabled(false);
    }

    private void readCodes() {
        if (!ObdManager.get().isConnected()) {
            showNotConnected();
            return;
        }
        readButton.setEnabled(false);
        showEmpty(R.string.dtc_reading);

        ObdManager.get().readDtcs(new ObdManager.ResultCallback<List<Dtc>>() {
            @Override
            public void onSuccess(List<Dtc> codes) {
                if (!isAdded()) {
                    return;
                }
                readButton.setEnabled(true);
                clearButton.setEnabled(true);
                bind(codes);
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) {
                    return;
                }
                readButton.setEnabled(true);
                showEmpty(R.string.dtc_none);
                Snackbar.make(requireView(), getString(R.string.read_failed_format, message),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void bind(List<Dtc> codes) {
        adapter.submit(codes);
        summary.setText(getString(R.string.dtc_count_format, codes.size()));
        breakdown.setText(breakdownText(codes));

        if (codes.isEmpty()) {
            showEmpty(R.string.dtc_none);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    /** "2 Stored - 1 Pending", omitting classes with nothing in them. */
    private String breakdownText(List<Dtc> codes) {
        StringBuilder builder = new StringBuilder();
        for (Dtc.Status status : Dtc.Status.values()) {
            int count = 0;
            for (Dtc dtc : codes) {
                if (dtc.getStatus() == status) {
                    count++;
                }
            }
            if (count == 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" \u00B7 ");
            }
            builder.append(count).append(" ").append(getString(status.getLabelRes()));
        }
        return builder.toString();
    }

    private void showEmpty(int messageRes) {
        empty.setText(messageRes);
        empty.setVisibility(View.VISIBLE);
    }

    private void confirmClear() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dtc_clear_title)
                .setMessage(R.string.dtc_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.dtc_clear, (dialog, which) -> clearCodes())
                .show();
    }

    private void clearCodes() {
        clearButton.setEnabled(false);
        ObdManager.get().clearDtcs(new ObdManager.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                if (!isAdded()) {
                    return;
                }
                Snackbar.make(requireView(), R.string.dtc_cleared, Snackbar.LENGTH_SHORT).show();
                readCodes();
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) {
                    return;
                }
                clearButton.setEnabled(true);
                Snackbar.make(requireView(), getString(R.string.read_failed_format, message),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDtcSelected(Dtc dtc) {
        ((MainActivity) requireActivity())
                .navigateTo(DtcDetailFragment.create(dtc.getCode(), dtc.getStatus()));
    }
}
