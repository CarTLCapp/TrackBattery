/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.model.core.table.TablePicture
import com.cartlc.tracker.fresh.model.flow.Stage

import java.io.File
import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */
class SqlTablePicture(
        private val dbSql: SQLiteDatabase
) : TablePicture {

    companion object {
        private val TAG = SqlTablePicture::class.simpleName

        private const val TABLE_NAME = "picture_collection"

        private const val KEY_ROWID = "_id"
        private const val KEY_COLLECTION_ID = "collection_id"
        private const val KEY_STAGE_ORD = "stage_ord"
        private const val KEY_FLOW_ELEMENT_ID = "flow_element_id"
        private const val KEY_PICTURE_FILENAME = "picture_filename"
        private const val KEY_UPLOADING_FILENAME = "uploading_filename"
        private const val KEY_UPLOADED = "uploaded"
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_COLLECTION_ID)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_STAGE_ORD)
        sbuf.append(" short default 0, ")
        sbuf.append(KEY_FLOW_ELEMENT_ID)
        sbuf.append(" integer default 0, ")
        sbuf.append(KEY_PICTURE_FILENAME)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADING_FILENAME)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    fun upgrade20() {
        try {
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_STAGE_ORD short default 0")
            dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_FLOW_ELEMENT_ID integer default 0")
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "upgrade20()", "db")
        }
    }

    override fun add(picture: File, collection_id: Long, stage: Stage): DataPicture {
        val item = DataPicture(
                unscaledFilename = picture.absolutePath,
                collectionId = collection_id,
                stage = stage)
        update(item)
        return item
    }

    override fun add(pictures: List<DataPicture>) {
        dbSql.beginTransaction()
        try {
            for (ele in pictures) {
                update(ele)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "add()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(picture_id: Long): DataPicture? {
        val selection = "$KEY_ROWID=?"
        val selectionArgs = arrayOf(picture_id.toString())
        val list = query(selection, selectionArgs)
        if (list.isNotEmpty()) {
            return list[0]
        }
        return null
    }

    override fun query(collection_id: Long, stage: Stage?): List<DataPicture> {
        val selection: String
        val selectionArgs: Array<String>
        if (stage != null) {
            selection = "$KEY_COLLECTION_ID=? AND $KEY_STAGE_ORD=? AND $KEY_FLOW_ELEMENT_ID=?"
            val stageOrd = stage.ord
            val flowElementId = if (stage is Stage.CUSTOM_FLOW) stage.flowElementId else 0
            selectionArgs = arrayOf(
                    collection_id.toString(),
                    stageOrd.toString(),
                    flowElementId.toString()
            )
        } else {
            selection = "$KEY_COLLECTION_ID=?"
            selectionArgs = arrayOf(collection_id.toString())
        }
        return query(selection, selectionArgs).toMutableList()
    }

    fun query(selection: String, selectionArgs: Array<String>): List<DataPicture> {
        val list = mutableListOf<DataPicture>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID)
            val idxStageOrd = cursor.getColumnIndex(KEY_STAGE_ORD)
            val idxFlowElementId = cursor.getColumnIndex(KEY_FLOW_ELEMENT_ID)
            val idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME)
            val idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME)
            val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idxRowId)
                val collectionId = cursor.getLong(idxCollectionId)
                val stageOrd = cursor.getInt(idxStageOrd)
                val flowElementId = cursor.getLong(idxFlowElementId)
                val stage = Stage.from(stageOrd, flowElementId)
                val filename = cursor.getString(idxPicture)
                val uploading = cursor.getString(idxUploading)
                val uploaded = cursor.getShort(idxUploaded).toInt() != 0
                val picture = DataPicture(id, filename, collectionId, stage, uploading, uploaded)
                list.add(picture)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "query()", "db")
        }
        return list
    }

    override fun removeFileDoesNotExist() {
        removeFileDoesNotExist(query(String(), emptyArray()))
    }

    override fun removeFileDoesNotExist(list: List<DataPicture>): List<DataPicture> {
        val filtered = ArrayList<DataPicture>()
        for (item in list) {
            if (item.existsUnscaled || item.existsScaled) {
                filtered.add(item)
            } else {
                remove(item)
            }
        }
        return filtered
    }

    @Synchronized
    override fun clearUploadedUnscaledPhotos() {
        val selection = "$KEY_UPLOADED=1"
        val list = query(selection, emptyArray())
        for (item in list) {
            if (item.existsUnscaled) {
                item.unscaledFile.delete()
            }
        }
    }

    override fun countPictures(collection_id: Long, stage: Stage?): Int {
        val selection: String
        val selectionArgs: Array<String>
        if (stage != null) {
            selection = "$KEY_COLLECTION_ID=? AND $KEY_STAGE_ORD=? AND $KEY_FLOW_ELEMENT_ID=?"
            val stageOrd = stage.ord
            val flowElementId = if (stage is Stage.CUSTOM_FLOW) stage.flowElementId else 0
            selectionArgs = arrayOf(
                    collection_id.toString(),
                    stageOrd.toString(),
                    flowElementId.toString()
            )
        } else {
            selection = "$KEY_COLLECTION_ID=?"
            selectionArgs = arrayOf(collection_id.toString())
        }
        return countPictures(selection, selectionArgs)
    }

    private fun countPictures(selection: String, selectionArgs: Array<String>): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_PICTURE_FILENAME, KEY_UPLOADING_FILENAME)
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME)
            val idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME)
            while (cursor.moveToNext()) {
                val unscaled = cursor.getString(idxPicture)
                val uploading = cursor.getString(idxUploading)
                val unscaledFile: File?
                val uploadingFile: File?
                if (unscaled != null) {
                    unscaledFile = File(unscaled)
                } else {
                    unscaledFile = null
                }
                if (uploading != null) {
                    uploadingFile = File(uploading)
                } else {
                    uploadingFile = null
                }
                if (unscaledFile != null && unscaledFile.exists() || uploadingFile != null && uploadingFile.exists()) {
                    count++
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "countPictures()", "db")
        }
        return count
    }

    override fun createCollectionFromPending(nextPictureCollectionID: Long): List<DataPicture> {
        val pending = removeFileDoesNotExist(query(0, null))
        val pictures = mutableListOf<DataPicture>()
        for (picture in pending) {
            pictures.add(DataPicture(picture, nextPictureCollectionID))
        }
        return pictures
    }

    override fun clearPendingPictures() {
        try {
            val where = "$KEY_COLLECTION_ID=0"
            dbSql.delete(TABLE_NAME, where, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "clearPendingPictures()", "db")
        }
    }

    fun remove(item: DataPicture) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "remove()", "db")
        }
    }

    override fun remove(collection_id: Long, stage: Stage) {
        try {
            val selection: String
            val selectionArgs: Array<String>
            if (stage is Stage.CUSTOM_FLOW) {
                selection = "$KEY_COLLECTION_ID=? AND $KEY_STAGE_ORD=? AND $KEY_FLOW_ELEMENT_ID=?"
                selectionArgs = arrayOf(collection_id.toString(), stage.ord.toString(), stage.flowElementId.toString())
            } else {
                selection = "$KEY_COLLECTION_ID=? AND $KEY_STAGE_ORD=?"
                selectionArgs = arrayOf(collection_id.toString(), stage.ord.toString())
            }
            dbSql.delete(TABLE_NAME, selection, selectionArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "remove($collection_id, $stage)", "db")
        }
    }

    @Synchronized
    override fun setUploaded(item: DataPicture) {
        item.uploaded = true
        update(item)
    }

    override fun update(item: DataPicture) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_PICTURE_FILENAME, item.unscaledFilename)
            values.put(KEY_UPLOADING_FILENAME, item.scaledFilename)
            values.put(KEY_UPLOADED, if (item.uploaded) 1 else 0)
            values.put(KEY_COLLECTION_ID, item.collectionId)
            values.put(KEY_STAGE_ORD, item.stage.ord)
            val flowElementId = if (item.stage is Stage.CUSTOM_FLOW) item.stage.flowElementId else 0
            values.put(KEY_FLOW_ELEMENT_ID, flowElementId)
            if (item.id > 0) {
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(item.id.toString())
                if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                    Timber.tag(TAG).e("Mysterious failure updating picture ID ${item.id}")
                    item.id = 0
                }
            }
            if (item.id == 0L) {
                item.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePicture::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}
