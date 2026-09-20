package com.falcon.car;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.falcon.car.ui.HomeFragment;
import com.falcon.car.ui.PlaceholderFragment;
import com.falcon.car.ui.VehicleSelectionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Single-activity host. Each bottom-nav tab owns a root fragment; detail screens
 * are pushed onto the back stack by {@link #navigateTo(Fragment)}.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    /** Replaces the tab root, dropping anything the previous tab had pushed. */
    private void showTab(int itemId) {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        manager.beginTransaction()
                .replace(R.id.nav_host, createTabFragment(itemId))
                .commit();
    }

    private Fragment createTabFragment(int itemId) {
        if (itemId == R.id.nav_diagnose) {
            return PlaceholderFragment.create(R.drawable.ic_diagnose,
                    R.string.placeholder_diagnose_title, R.string.placeholder_diagnose_desc, 2);
        }
        if (itemId == R.id.nav_live_data) {
            return PlaceholderFragment.create(R.drawable.ic_live_data,
                    R.string.placeholder_live_title, R.string.placeholder_live_desc, 2);
        }
        if (itemId == R.id.nav_vehicles) {
            return new VehicleSelectionFragment();
        }
        if (itemId == R.id.nav_more) {
            return PlaceholderFragment.create(R.drawable.ic_more,
                    R.string.placeholder_more_title, R.string.placeholder_more_desc, 3);
        }
        return new HomeFragment();
    }

    /** Pushes a detail screen, keeping the current tab selected. */
    public void navigateTo(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host, fragment)
                .addToBackStack(null)
                .commit();
    }

    /** Jumps to a tab root, e.g. when the home screen hands off to Diagnose. */
    public void selectTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }
}
