package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */
public class TableAddress {

    static TableAddress sInstance;

    static void Init(SQLiteDatabase db) {
        new TableAddress(db);
    }

    public static TableAddress getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_address";

    static final String KEY_ROWID = "_id";
    static final String KEY_COMPANY = "company";
    static final String KEY_STREET = "street";
    static final String KEY_CITY = "city";
    static final String KEY_STATE = "state";
    static final String KEY_SERVER_ID = "server_id";
    static final String KEY_DISABLED = "disabled";

    final SQLiteDatabase mDb;

    public TableAddress(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
        }
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_COMPANY);
        sbuf.append(" text, ");
        sbuf.append(KEY_STREET);
        sbuf.append(" text, ");
        sbuf.append(KEY_CITY);
        sbuf.append(" text, ");
        sbuf.append(KEY_STATE);
        sbuf.append(" text, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
        sbuf.append(KEY_DISABLED);
        sbuf.append(" bit)");
        mDb.execSQL(sbuf.toString());

        Timber.d("MYDEBUG TableAddress.CREATE: " + sbuf.toString());
    }

    public void add(List<DataAddress> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (DataAddress address : list) {
                values.clear();
                values.put(KEY_COMPANY, address.company);
                values.put(KEY_STREET, address.street);
                values.put(KEY_CITY, address.city);
                values.put(KEY_STATE, address.state);
                values.put(KEY_SERVER_ID, address.server_id);
                values.put(KEY_DISABLED, address.disabled ? 1 : 0);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public long add(DataAddress address) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COMPANY, address.company);
            values.put(KEY_STREET, address.street);
            values.put(KEY_CITY, address.city);
            values.put(KEY_STATE, address.state);
            values.put(KEY_SERVER_ID, address.server_id);
            values.put(KEY_DISABLED, address.disabled ? 1 : 0);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public void update(DataAddress address) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COMPANY, address.company);
            values.put(KEY_STREET, address.street);
            values.put(KEY_CITY, address.city);
            values.put(KEY_STATE, address.state);
            values.put(KEY_SERVER_ID, address.server_id);
            values.put(KEY_DISABLED, address.disabled ? 1 : 0);
            String where = KEY_ROWID + "=?";
            String [] whereArgs = { Long.toString(address.id) };
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }


    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(true, TABLE_NAME, null, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public DataAddress query(long id) {
        DataAddress address = null;
        try {
            final String[] columns = {KEY_STATE, KEY_CITY, KEY_COMPANY, KEY_STREET, KEY_SERVER_ID, KEY_DISABLED};
            final String orderBy = KEY_COMPANY + " ASC";
            final String selection = KEY_ROWID + " =?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxState = cursor.getColumnIndex(KEY_STATE);
            int idxCity = cursor.getColumnIndex(KEY_CITY);
            int idxStreet = cursor.getColumnIndex(KEY_STREET);
            int idxCompany = cursor.getColumnIndex(KEY_COMPANY);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            if (cursor.moveToFirst()) {
                address = new DataAddress(cursor.getInt(idxServerId),
                        cursor.getString(idxCompany),
                        cursor.getString(idxStreet),
                        cursor.getString(idxCity),
                        cursor.getString(idxState));
                address.id = id;
                address.disabled = cursor.getShort(idxDisabled) == 1;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return address;
    }

    public List<DataAddress> query() {
        ArrayList<DataAddress> list = new ArrayList();
        try {
            final String[] columns = {KEY_STATE, KEY_CITY, KEY_COMPANY, KEY_STREET, KEY_ROWID, KEY_SERVER_ID, KEY_DISABLED};
            final String orderBy = KEY_COMPANY + " ASC";
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, orderBy, null);
            int idxState = cursor.getColumnIndex(KEY_STATE);
            int idxCity = cursor.getColumnIndex(KEY_CITY);
            int idxStreet = cursor.getColumnIndex(KEY_STREET);
            int idxCompany = cursor.getColumnIndex(KEY_COMPANY);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            while (cursor.moveToNext()) {
                DataAddress address;
                list.add(address = new DataAddress(cursor.getLong(idxRowId),
                        cursor.getInt(idxServerId),
                        cursor.getString(idxCompany),
                        cursor.getString(idxStreet),
                        cursor.getString(idxCity),
                        cursor.getString(idxState)));
                address.disabled = cursor.getShort(idxDisabled) == 1;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryStates(String company) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_STATE};
            final String orderBy = KEY_STATE + " ASC";
            String selection;
            String[] selectionArgs;
            if (company == null) {
                selection = null;
                selectionArgs = null;
            } else {
                selection = KEY_COMPANY + " =?";
                selectionArgs = new String[]{company};
            }
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_STATE);
            String state;
            while (cursor.moveToNext()) {
                state = cursor.getString(idxValue);
                list.add(state);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCities(String state) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_CITY};
            final String orderBy = KEY_CITY + " ASC";
            final String selection = KEY_STATE + " =?";
            final String[] selectionArgs = {state};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_CITY);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCompanies() {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_COMPANY};
            final String orderBy = KEY_COMPANY + " ASC";
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_COMPANY);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryStreets(String company, String city, String state) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_STREET};
            final String orderBy = KEY_STREET + " ASC";
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_STATE);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_CITY);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_COMPANY);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {state, city, company};
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_STREET);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public long queryAddressId(String company, String street, String city, String state) {
        long id = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_COMPANY);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_STATE);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_CITY);
            sbuf.append(" =? AND ");
            sbuf.append(KEY_STREET);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {company, state, city, street};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(idxRowId);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return id;
    }

    public DataAddress queryByServerId(int serverId) {
        DataAddress data = null;
        try {
            final String[] columns = {KEY_COMPANY, KEY_STREET, KEY_CITY, KEY_STATE, KEY_ROWID, KEY_DISABLED};
            final String selection = KEY_SERVER_ID + "=?";
            final String[] selectionArgs = {Integer.toString(serverId)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxState = cursor.getColumnIndex(KEY_STATE);
            int idxCity = cursor.getColumnIndex(KEY_CITY);
            int idxStreet = cursor.getColumnIndex(KEY_STREET);
            int idxCompany = cursor.getColumnIndex(KEY_COMPANY);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            if (cursor.moveToFirst()) {
                data = new DataAddress(cursor.getLong(idxRowId),
                        serverId,
                        cursor.getString(idxCompany),
                        cursor.getString(idxStreet),
                        cursor.getString(idxCity),
                        cursor.getString(idxState));
                data.disabled = cursor.getShort(idxDisabled) == 1;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return data;
    }

    public void remove(long id) {
        try {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(id)};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void removeOrDisable(DataAddress item) {
        if (TableEntry.getInstance().countAddresses(item.id) == 0) {
            // No entries for this, so just remove.
            remove(item.id);
        } else {
            item.disabled = true;
            update(item);
        }
    }
}
