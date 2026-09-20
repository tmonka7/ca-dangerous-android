package com.falcon.car.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.falcon.car.R;
import com.falcon.car.data.DtcDecoder;
import com.falcon.car.data.model.Dtc;
import com.falcon.car.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;

/** Rows for the trouble code list. */
public class DtcAdapter extends RecyclerView.Adapter<DtcAdapter.DtcHolder> {

    public interface OnDtcSelected {
        void onDtcSelected(Dtc dtc);
    }

    private final List<Dtc> codes = new ArrayList<>();
    private final OnDtcSelected listener;

    public DtcAdapter(OnDtcSelected listener) {
        this.listener = listener;
    }

    public void submit(List<Dtc> newCodes) {
        codes.clear();
        codes.addAll(newCodes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DtcHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dtc, parent, false);
        return new DtcHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DtcHolder holder, int position) {
        holder.bind(codes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return codes.size();
    }

    static class DtcHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView code;
        private final TextView description;
        private final TextView system;
        private final TextView status;

        DtcHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.dtc_icon);
            code = itemView.findViewById(R.id.dtc_code);
            description = itemView.findViewById(R.id.dtc_description);
            system = itemView.findViewById(R.id.dtc_system);
            status = itemView.findViewById(R.id.dtc_status);
        }

        void bind(Dtc dtc, OnDtcSelected listener) {
            Context context = itemView.getContext();
            Dtc.Status dtcStatus = dtc.getStatus();

            code.setText(dtc.getCode());
            description.setText(DtcDecoder.describe(context, dtc.getCode()));
            system.setText(systemLine(context, dtc.getCode()));

            UiUtils.setIcon(icon, iconFor(dtcStatus), dtcStatus.getColorRes());

            status.setText(dtcStatus.getLabelRes());
            UiUtils.stylePill(status, pillFor(dtcStatus), dtcStatus.getColorRes());

            itemView.setOnClickListener(v -> listener.onDtcSelected(dtc));
        }

        /** "Powertrain - Generic (SAE)", so the row says where the code comes from. */
        private String systemLine(Context context, String code) {
            String system = context.getString(DtcDecoder.systemRes(code));
            String kind = context.getString(DtcDecoder.isManufacturerSpecific(code)
                    ? R.string.dtc_kind_manufacturer
                    : R.string.dtc_kind_generic);
            return system + " \u00B7 " + kind;
        }

        private int iconFor(Dtc.Status status) {
            switch (status) {
                case PENDING:
                    return R.drawable.ic_warning;
                case PERMANENT:
                    return R.drawable.ic_pending;
                default:
                    return R.drawable.ic_error;
            }
        }

        private int pillFor(Dtc.Status status) {
            switch (status) {
                case PENDING:
                    return R.drawable.bg_pill_warning;
                case PERMANENT:
                    return R.drawable.bg_pill_neutral;
                default:
                    return R.drawable.bg_pill_fault;
            }
        }
    }
}
