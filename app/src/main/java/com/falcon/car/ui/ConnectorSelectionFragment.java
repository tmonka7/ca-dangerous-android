package com.falcon.car.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falcon.car.R;
import com.falcon.car.data.SessionState;
import com.falcon.car.data.model.ConnectorType;
import com.falcon.car.data.model.Protocol;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 05 - the physical socket. Groups and tiles are generated from
 * {@link ConnectorType}, so a new connector is a data entry rather than a
 * layout change.
 */
public class ConnectorSelectionFragment extends Fragment {

    private final List<View> tiles = new ArrayList<>();
    private final List<ConnectorType> tileTypes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connector_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View header = view.findViewById(R.id.header);
        ((TextView) header.findViewById(R.id.header_title))
                .setText(R.string.title_connector_selection);
        header.findViewById(R.id.header_back).setOnClickListener(
                v -> getParentFragmentManager().popBackStack());

        buildGroups(view.findViewById(R.id.connector_groups));
    }

    private void buildGroups(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing) / 2;

        container.removeAllViews();
        tiles.clear();
        tileTypes.clear();

        for (ConnectorType.Group group : ConnectorType.Group.values()) {
            View section = inflater.inflate(R.layout.item_connector_group, container, false);
            ((TextView) section.findViewById(R.id.group_title)).setText(group.getTitleRes());

            GridLayout grid = section.findViewById(R.id.group_grid);
            for (ConnectorType type : ConnectorType.values()) {
                if (type.getGroup() != group) {
                    continue;
                }
                View tile = buildTile(inflater, grid, type);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(spacing, spacing, spacing, spacing);
                tile.setLayoutParams(params);

                grid.addView(tile);
                tiles.add(tile);
                tileTypes.add(type);
            }
            container.addView(section);
        }
    }

    private View buildTile(LayoutInflater inflater, ViewGroup parent, ConnectorType type) {
        View tile = inflater.inflate(R.layout.item_connector_tile, parent, false);

        UiUtils.setIcon(tile.findViewById(R.id.connector_icon), type.getIconRes(),
                R.color.redline_red);
        ((TextView) tile.findViewById(R.id.connector_name)).setText(type.getDisplayName());
        ((TextView) tile.findViewById(R.id.connector_pins))
                .setText(getString(R.string.pin_count_format, type.getPinCount()));
        ((TextView) tile.findViewById(R.id.connector_protocols))
                .setText(protocolSummary(type));

        tile.setSelected(type == SessionState.get().getConnector());
        tile.setOnClickListener(v -> select(type));
        return tile;
    }

    private String protocolSummary(ConnectorType type) {
        StringBuilder builder = new StringBuilder();
        for (Protocol protocol : type.getProtocols()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(protocol.getDisplayName());
        }
        return builder.toString();
    }

    private void select(ConnectorType type) {
        SessionState.get().setConnector(type);
        for (int i = 0; i < tiles.size(); i++) {
            tiles.get(i).setSelected(tileTypes.get(i) == type);
        }
        Snackbar.make(requireView(),
                getString(R.string.connector_selected_format, type.getDisplayName()),
                Snackbar.LENGTH_SHORT).show();
    }
}
