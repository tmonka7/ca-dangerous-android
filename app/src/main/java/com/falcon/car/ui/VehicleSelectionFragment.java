package com.falcon.car.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falcon.car.MainActivity;
import com.falcon.car.R;
import com.falcon.car.data.MockData;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.Vehicle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;
import java.util.Locale;

/**
 * Screen 06 - pick the vehicle, either from recent work or by drilling down
 * make, model, year and engine.
 */
public class VehicleSelectionFragment extends Fragment {

    private static final String ARG_DETAIL = "detail";

    /** Pushed onto the back stack from another screen, so it shows a back arrow. */
    public static VehicleSelectionFragment asDetail() {
        VehicleSelectionFragment fragment = new VehicleSelectionFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DETAIL, true);
        fragment.setArguments(args);
        return fragment;
    }

    private LinearLayout recentContainer;
    private MaterialAutoCompleteTextView makeInput;
    private MaterialAutoCompleteTextView modelInput;
    private MaterialAutoCompleteTextView yearInput;
    private MaterialAutoCompleteTextView engineInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vehicle_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title))
                .setText(R.string.title_vehicle_selection);

        View back = header.findViewById(R.id.header_back);
        // As a tab root there is nothing to go back to, so drop the arrow.
        boolean isDetail = getArguments() != null && getArguments().getBoolean(ARG_DETAIL);
        if (isDetail) {
            back.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        } else {
            back.setVisibility(View.GONE);
        }

        recentContainer = view.findViewById(R.id.recent_container);
        makeInput = view.findViewById(R.id.input_make);
        modelInput = view.findViewById(R.id.input_model);
        yearInput = view.findViewById(R.id.input_year);
        engineInput = view.findViewById(R.id.input_engine);

        MaterialButton autoVin = view.findViewById(R.id.btn_auto_vin);
        autoVin.setOnClickListener(
                v -> ((MainActivity) requireActivity()).navigateTo(new VinScannerFragment()));

        setUpDropdowns();
        bindRecent(MockData.recentVehicles());
        watchSearch(view.findViewById(R.id.vehicle_search));
        prefillFrom(SessionState.get().getVehicle());
    }

    private void setUpDropdowns() {
        makeInput.setSimpleItems(MockData.makes().toArray(new String[0]));
        yearInput.setSimpleItems(MockData.years().toArray(new String[0]));
        engineInput.setSimpleItems(MockData.engines().toArray(new String[0]));

        makeInput.setOnItemClickListener((parent, view, position, id) -> {
            String make = makeInput.getText().toString();
            modelInput.setText("", false);
            modelInput.setSimpleItems(MockData.modelsFor(make).toArray(new String[0]));
        });

        engineInput.setOnItemClickListener((parent, view, position, id) -> commitSelection());
        yearInput.setOnItemClickListener((parent, view, position, id) -> commitSelection());
    }

    /** Writes whatever is filled in back into the session. */
    private void commitSelection() {
        String make = makeInput.getText().toString();
        String model = modelInput.getText().toString();
        String yearText = yearInput.getText().toString();
        String engine = engineInput.getText().toString();

        if (make.isEmpty() || model.isEmpty() || yearText.isEmpty()) {
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            return;
        }

        Vehicle vehicle = new Vehicle(make, model, year, engine, "", "");
        SessionState.get().setVehicle(vehicle);
        Snackbar.make(requireView(),
                getString(R.string.vehicle_selected_format, vehicle.getDisplayName()),
                Snackbar.LENGTH_SHORT).show();
    }

    private void prefillFrom(Vehicle vehicle) {
        makeInput.setText(vehicle.getMake(), false);
        modelInput.setSimpleItems(MockData.modelsFor(vehicle.getMake()).toArray(new String[0]));
        modelInput.setText(vehicle.getModel(), false);
        yearInput.setText(String.valueOf(vehicle.getYear()), false);
        engineInput.setText(vehicle.getEngine(), false);
    }

    private void bindRecent(List<Vehicle> vehicles) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        recentContainer.removeAllViews();

        for (Vehicle vehicle : vehicles) {
            View row = inflater.inflate(R.layout.item_recent_vehicle, recentContainer, false);
            ((TextView) row.findViewById(R.id.recent_name)).setText(vehicle.getDisplayName());
            ((TextView) row.findViewById(R.id.recent_spec)).setText(vehicle.getSpecLine());
            ((TextView) row.findViewById(R.id.recent_scan))
                    .setText(getString(R.string.last_scan_format, vehicle.getLastScan()));
            row.setOnClickListener(v -> selectRecent(vehicle));
            recentContainer.addView(row);
        }
    }

    private void selectRecent(Vehicle vehicle) {
        SessionState.get().setVehicle(vehicle);
        prefillFrom(vehicle);
        Snackbar.make(requireView(),
                getString(R.string.vehicle_selected_format, vehicle.getDisplayName()),
                Snackbar.LENGTH_SHORT).show();
    }

    private void watchSearch(EditText search) {
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bindRecent(filterRecent(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private List<Vehicle> filterRecent(String query) {
        List<Vehicle> all = MockData.recentVehicles();
        if (query.trim().isEmpty()) {
            return all;
        }
        String needle = query.toLowerCase(Locale.getDefault());
        List<Vehicle> matches = new java.util.ArrayList<>();
        for (Vehicle vehicle : all) {
            if (vehicle.getDisplayName().toLowerCase(Locale.getDefault()).contains(needle)) {
                matches.add(vehicle);
            }
        }
        return matches;
    }
}
