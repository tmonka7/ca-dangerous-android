package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.falcon.car.BuildConfig;
import com.falcon.car.R;
import com.falcon.car.data.AppPrefs;
import com.falcon.car.data.DtcDictionary;
import com.falcon.car.obd.ObdManager;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Screen 15 - language, demo mode, and what the current link is actually
 * talking to.
 */
public class SettingsFragment extends Fragment {

    /** Language tag for {@link AppCompatDelegate}; empty means follow the system. */
    private static final String[] LANGUAGE_TAGS = {"", "en", "ja", "zh-CN"};

    private LinearLayout languageContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.title_settings);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);

        languageContainer = view.findViewById(R.id.language_container);
        buildLanguageOptions();

        MaterialSwitch demoSwitch = view.findViewById(R.id.switch_demo);
        demoSwitch.setChecked(AppPrefs.isDemoMode(requireContext()));
        demoSwitch.setOnCheckedChangeListener((button, checked) -> {
            AppPrefs.setDemoMode(requireContext(), checked);
            // The live link belongs to the mode that opened it.
            ObdManager.get().disconnect();
        });

        bindRow(view.findViewById(R.id.row_adapter), getString(R.string.settings_adapter),
                adapterText());
        bindRow(view.findViewById(R.id.row_protocol), getString(R.string.settings_protocol),
                protocolText());
        bindRow(view.findViewById(R.id.row_dictionary), getString(R.string.settings_dictionary),
                getString(R.string.settings_dictionary_format,
                        DtcDictionary.size(requireContext())));
        bindRow(view.findViewById(R.id.row_version), getString(R.string.settings_version),
                BuildConfig.VERSION_NAME);
    }

    private String adapterText() {
        String name = ObdManager.get().getAdapterName();
        return name.isEmpty() ? getString(R.string.status_disconnected) : name;
    }

    private String protocolText() {
        String protocol = ObdManager.get().getProtocolName();
        return protocol.isEmpty() ? getString(R.string.status_disconnected) : protocol;
    }

    private void buildLanguageOptions() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        String current = AppCompatDelegate.getApplicationLocales().toLanguageTags();

        languageContainer.removeAllViews();
        for (int i = 0; i < LANGUAGE_TAGS.length; i++) {
            String tag = LANGUAGE_TAGS[i];
            View row = inflater.inflate(R.layout.item_language_option, languageContainer, false);

            ((TextView) row.findViewById(R.id.language_name)).setText(nameFor(i));
            row.findViewById(R.id.language_check)
                    .setVisibility(matches(current, tag) ? View.VISIBLE : View.INVISIBLE);
            row.setOnClickListener(v -> applyLanguage(tag));

            languageContainer.addView(row);
        }
    }

    private String nameFor(int index) {
        switch (index) {
            case 1:
                return "English";
            case 2:
                return "\u65E5\u672C\u8A9E";
            case 3:
                return "\u7B80\u4F53\u4E2D\u6587";
            default:
                return getString(R.string.settings_language_system);
        }
    }

    /** An empty stored tag means "system default", which is the first row. */
    private boolean matches(String current, String tag) {
        if (tag.isEmpty()) {
            return current.isEmpty();
        }
        return current.toLowerCase().startsWith(tag.toLowerCase());
    }

    /**
     * AppCompat stores the choice and recreates the activity, so the whole UI
     * and the trouble code dictionary switch together.
     */
    private void applyLanguage(String tag) {
        LocaleListCompat locales = tag.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    private void bindRow(View row, String label, String value) {
        ((TextView) row.findViewById(R.id.row_label)).setText(label);
        ((TextView) row.findViewById(R.id.row_value)).setText(value);
    }
}
