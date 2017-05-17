package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataProjectGroup;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableNote;
import com.cartlc.tracker.data.TableNoteProjectCollection;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class NoteListEntryAdapter extends RecyclerView.Adapter<NoteListEntryAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.entry) EditText entry;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context        mContext;
    protected       List<DataNote> mItems;

    public NoteListEntryAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_entry_note, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataNote item = mItems.get(position);
        holder.label.setText(item.name);
        holder.entry.setText(item.value);
        holder.entry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    item.value = holder.entry.getText().toString();
                    TableNote.getInstance().updateValue(item);
                }
            }
        });
        if (item.type == DataNote.Type.ALPHANUMERIC) {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.NUMERIC_WITH_SPACES) {
            holder.entry.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.NUMERIC) {
            holder.entry.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.MULTILINE) {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            holder.entry.setMaxLines(3);
        } else {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            holder.entry.setMaxLines(1);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectGroup curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        mItems = TableNoteProjectCollection.getInstance().getNotes(curGroup.projectNameId);
        notifyDataSetChanged();
    }

}
