package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.falcon.car.R;

/** Stands in for a screen scheduled for a later phase, and says which one. */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_ICON = "icon";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_PHASE = "phase";

    public static PlaceholderFragment create(@DrawableRes int iconRes, @StringRes int titleRes,
                                             @StringRes int descRes, int phase) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON, iconRes);
        args.putInt(ARG_TITLE, titleRes);
        args.putInt(ARG_DESC, descRes);
        args.putInt(ARG_PHASE, phase);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        ImageView icon = view.findViewById(R.id.placeholder_icon);
        icon.setImageResource(args.getInt(ARG_ICON));
        UiUtils.tint(icon, R.color.redline_red);

        ((TextView) view.findViewById(R.id.placeholder_title)).setText(args.getInt(ARG_TITLE));
        ((TextView) view.findViewById(R.id.placeholder_desc)).setText(args.getInt(ARG_DESC));
        ((TextView) view.findViewById(R.id.placeholder_phase))
                .setText(getString(R.string.placeholder_phase_format, args.getInt(ARG_PHASE)));
    }
}
