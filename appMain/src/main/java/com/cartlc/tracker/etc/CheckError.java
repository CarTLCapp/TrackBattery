package com.cartlc.tracker.etc;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cartlc.tracker.R;
import com.cartlc.tracker.act.MainActivity;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataTruck;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.event.EventRefreshProjects;

import java.util.List;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by dug on 10/14/17.
 */

public class CheckError {

    public interface CheckErrorResult {
        void doEdit();
    }

    static CheckError sInstance;

    public static CheckError getInstance() {
        return sInstance;
    }

    public static void Init() {
        new CheckError();
    }

    AlertDialog mDialog;

    CheckError() {
        sInstance = this;
    }

    public void cleanup() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    void cleanup(DialogInterface dialog) {
        dialog.dismiss();
        mDialog = null;
    }

    /**
     *
     * @param act
     * @return true if there was an error detected.
     */
    public boolean checkErrors(Activity act, CheckErrorResult callback) {
        List<DataEntry> entries = TableEntry.getInstance().query();
        for (DataEntry entry : entries) {
            if (!hasTruck(entry)) {
                showTruckError(act, entry, callback);
                return true;
            }
        }
        return false;
    }

    boolean hasTruck(DataEntry entry) {
        DataTruck truck = entry.getTruck();
        if (truck != null) {
            if (truck.truckNumber > 0) {
                return true;
            }
            if (truck.licensePlateNumber != null) {
                return true;
            }
        }
        return false;
    }

    void showTruckError(final Activity act, final DataEntry entry, final CheckErrorResult callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.title_error);
        builder.setMessage(getMissingTruckError(act, entry));
        builder.setPositiveButton(R.string.btn_edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefHelper.getInstance().setFromEntry(entry);
                cleanup(dialog);
                callback.doEdit();
            }
        });
        builder.setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cleanup(dialog);
                showConfirmDelete(act, entry);
            }
        });
        builder.setNeutralButton(R.string.btn_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cleanup(dialog);
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    String getMissingTruckError(final Activity act, DataEntry entry) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(act.getString(R.string.error_missing_truck_long));
        sbuf.append("\n  ");
        sbuf.append(act.getString(R.string.title_project_));
        sbuf.append(" ");
        sbuf.append(entry.getProjectName());
        sbuf.append("\n  ");
        sbuf.append(entry.getAddressLine());
        sbuf.append("\n  ");
        sbuf.append(act.getString(R.string.title_status_));
        sbuf.append(" ");
        sbuf.append(entry.getStatus(act));
        sbuf.append("\n  ");
        sbuf.append(act.getString(R.string.title_notes_));
        sbuf.append(" ");
        sbuf.append(entry.getNotesLine());
        sbuf.append("\n  ");
        sbuf.append(act.getString(R.string.title_equipment_installed_));
        sbuf.append(entry.getEquipmentLine(act));
        return sbuf.toString();
    }

    void showConfirmDelete(Activity act, final DataEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.title_confirmation);
        builder.setMessage(R.string.dialog_confirm_delete);
        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cleanup(dialog);
                TableEntry.getInstance().remove(entry);
                EventBus.getDefault().post(new EventRefreshProjects());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cleanup(dialog);
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

}