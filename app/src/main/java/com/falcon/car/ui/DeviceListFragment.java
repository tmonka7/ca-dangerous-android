package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.R;
import com.falcon.car.data.AdapterCatalog;
import com.falcon.car.data.model.AdapterProfile;
import com.falcon.car.data.model.ConnectorType;
import com.falcon.car.data.model.Protocol;
import com.falcon.car.data.model.SupportLevel;
import com.falcon.car.ui.adapter.AdapterCatalogAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Screen 04 - the adapter catalogue. Tapping a family opens its capability
 * sheet, which is the screen that keeps the app honest about what each VCI can
 * and cannot reach.
 */
public class DeviceListFragment extends Fragment
        implements AdapterCatalogAdapter.OnProfileSelected {

    private AdapterCatalogAdapter catalogAdapter;
    private ChipGroup filterGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_device_list);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        RecyclerView list = view.findViewById(R.id.adapter_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        catalogAdapter = new AdapterCatalogAdapter(this);
        list.setAdapter(catalogAdapter);

        filterGroup = view.findViewById(R.id.filter_group);
        buildFilters();
        applyFilter(null);
    }

    private void buildFilters() {
        addFilterChip(R.string.filter_all, null, true);
        addFilterChip(R.string.filter_obd2, AdapterProfile.Category.OBD2, false);
        addFilterChip(R.string.filter_heavy_duty, AdapterProfile.Category.HEAVY_DUTY, false);
        addFilterChip(R.string.filter_j2534, AdapterProfile.Category.J2534, false);
    }

    private void addFilterChip(int labelRes, @Nullable AdapterProfile.Category category,
                               boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(requireContext())
                .inflate(R.layout.item_filter_chip, filterGroup, false);
        chip.setId(View.generateViewId());
        chip.setText(labelRes);
        chip.setChecked(checked);
        chip.setOnClickListener(v -> applyFilter(category));
        filterGroup.addView(chip);
    }

    private void applyFilter(@Nullable AdapterProfile.Category category) {
        catalogAdapter.submit(AdapterCatalog.filtered(category));
    }

    @Override
    public void onProfileSelected(AdapterProfile profile) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_adapter_detail, null, false);

        ((TextView) content.findViewById(R.id.detail_models_label)).setText(profile.getBrand());
        ((TextView) content.findViewById(R.id.detail_models))
                .setText(joinModels(profile));
        ((TextView) content.findViewById(R.id.detail_transports))
                .setText(AdapterCatalogAdapter.transportSummary(requireContext(), profile));
        ((TextView) content.findViewById(R.id.detail_connectors))
                .setText(joinConnectors(profile));

        fillProtocolRows(content.findViewById(R.id.detail_protocols), profile);

        new AlertDialog.Builder(requireContext())
                .setTitle(profile.getBrand())
                .setView(content)
                .setPositiveButton(R.string.action_close, null)
                .show();
    }

    /** Every protocol we model, with this family's level against each one. */
    private void fillProtocolRows(LinearLayout container, AdapterProfile profile) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        container.removeAllViews();

        for (Protocol protocol : Protocol.values()) {
            SupportLevel level = profile.supportFor(protocol);
            View row = inflater.inflate(R.layout.item_protocol_row, container, false);

            ((TextView) row.findViewById(R.id.protocol_name)).setText(protocol.getDisplayName());

            TextView support = row.findViewById(R.id.protocol_support);
            support.setText(level.getLabelRes());
            support.setTextColor(ContextCompat.getColor(requireContext(), level.getColorRes()));

            container.addView(row);
        }
    }

    private String joinModels(AdapterProfile profile) {
        StringBuilder builder = new StringBuilder();
        for (String model : profile.getModels()) {
            if (builder.length() > 0) {
                builder.append(" \u00B7 ");
            }
            builder.append(model);
        }
        return builder.toString();
    }

    private String joinConnectors(AdapterProfile profile) {
        StringBuilder builder = new StringBuilder();
        for (ConnectorType connector : profile.getConnectors()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(connector.getDisplayName())
                    .append(" ")
                    .append(getString(R.string.pin_count_format, connector.getPinCount()));
        }
        return builder.toString();
    }
}
