package com.cartlc.tracker.act;

import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.etc.EntryStatus;
import com.cartlc.tracker.etc.PrefHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class EntryListAdapter extends RecyclerView.Adapter<EntryListAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.project_name)    TextView projectName;
        @BindView(R.id.truck_value)     TextView truckValue;
        @BindView(R.id.project_address) TextView projectAddress;
        @BindView(R.id.status)          TextView status;
        @BindView(R.id.notes)           TextView notes;
        @BindView(R.id.equipments)      TextView equipments;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemSelectedListener {
        void onSelected(DataEntry entry);
    }

    final Context                mContext;
    final OnItemSelectedListener mListener;
    List<DataEntry> mItems;
    HashMap<Long, EntryStatus> mStatusMap = new HashMap();
    int mSelectedPos;

    public EntryListAdapter(Context context, OnItemSelectedListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_full, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataEntry item = mItems.get(position);
        holder.projectName.setText(item.getProjectName());
        holder.truckValue.setText(item.getTruck().toString());
        holder.projectAddress.setText(item.getAddressLine());
        holder.status.setText(getStatus(item).getLine(mContext));
        holder.notes.setText(item.getNotesLine());
        holder.equipments.setText(getEquipmentNeeded(item));
        if (TextUtils.isEmpty(holder.notes.getText().toString().trim())) {
            holder.notes.setVisibility(View.GONE);
        } else {
            holder.notes.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPos = position;
                if (mListener != null) {
                    mListener.onSelected(item);
                }
                notifyDataSetChanged();
            }
        });
        if (mSelectedPos == position) {
            mSelectedPos = position;
            holder.itemView.setBackgroundResource(R.color.project_highlight);
        } else {
            holder.itemView.setBackgroundResource(R.color.project_normal);
        }
    }

    EntryStatus getStatus(DataEntry item) {
        if (!mStatusMap.containsKey(item.id)) {
            mStatusMap.put(item.id, item.computeStatus());
        }
        return mStatusMap.get(item.id);
    }

    String getEquipmentNeeded(DataEntry item) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(mContext.getString(R.string.status_partial_install_equipment_needed));
        sbuf.append(getStatus(item).getEquipmentNeeded());
        return sbuf.toString();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        mStatusMap.clear();
        DataProjectAddressCombo combo = PrefHelper.getInstance().getCurrentProjectGroup();
        if (combo == null) {
            mItems = new ArrayList();
        } else {
            mItems = combo.getEntries();
        }
    }
}
