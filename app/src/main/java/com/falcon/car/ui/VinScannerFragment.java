package com.falcon.car.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.falcon.car.R;
import com.falcon.car.data.MockData;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.Vehicle;
import com.falcon.car.util.VinValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Screen 07 - VIN capture. The viewfinder and the validation are real; the
 * camera and OCR are wired in a later phase, so Scan replays a known VIN and
 * manual entry is the working path.
 */
public class VinScannerFragment extends Fragment {

    private static final long FAKE_SCAN_MS = 1200L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView vinPreview;
    private MaterialButton scanButton;
    @Nullable
    private ObjectAnimator scanLineAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vin_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_vin_scanner);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        vinPreview = view.findViewById(R.id.vin_preview);
        scanButton = view.findViewById(R.id.btn_scan);

        scanButton.setOnClickListener(v -> simulateScan());
        view.findViewById(R.id.btn_manual_vin).setOnClickListener(v -> showManualEntry());

        startScanLine(view.findViewById(R.id.vin_scan_line));
    }

    private void startScanLine(View line) {
        float travel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26f,
                getResources().getDisplayMetrics());
        ObjectAnimator animator = ObjectAnimator.ofFloat(line, View.TRANSLATION_Y, -travel, travel);
        animator.setDuration(1400L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
        scanLineAnimator = animator;
    }

    /** Stands in for OCR until the camera pipeline lands. */
    private void simulateScan() {
        scanButton.setEnabled(false);
        vinPreview.setText(R.string.vin_scanning);

        handler.postDelayed(() -> {
            scanButton.setEnabled(true);
            String vin = MockData.recentVehicles().get(0).getVin();
            vinPreview.setText(vin);
            vinPreview.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            applyVin(vin);
        }, FAKE_SCAN_MS);
    }

    private void showManualEntry() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_vin_entry, null, false);
        TextInputLayout inputLayout = content.findViewById(R.id.vin_input_layout);
        TextInputEditText input = content.findViewById(R.id.vin_input);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.vin_dialog_title)
                .setView(content)
                .setPositiveButton(R.string.action_ok, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.show();
        // Bound after show() so a rejected VIN keeps the dialog open.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String raw = input.getText() == null ? "" : input.getText().toString();
            VinValidator.Result result = VinValidator.validate(raw);
            if (!result.isValid()) {
                inputLayout.setError(getString(result.errorRes));
                return;
            }
            dialog.dismiss();
            String vin = VinValidator.normalize(raw);
            vinPreview.setText(vin);
            applyVin(vin);
        });
    }

    /** Stores the VIN against the session vehicle and reports any soft warning. */
    private void applyVin(String vin) {
        Vehicle current = SessionState.get().getVehicle();
        SessionState.get().setVehicle(new Vehicle(current.getMake(), current.getModel(),
                current.getYear(), current.getEngine(), vin, current.getLastScan()));

        VinValidator.Result result = VinValidator.validate(vin);
        String message = result.warningRes != 0
                ? getString(result.warningRes)
                : getString(R.string.vin_accepted_format, vin);
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        if (scanLineAnimator != null) {
            scanLineAnimator.cancel();
            scanLineAnimator = null;
        }
        super.onDestroyView();
    }
}
