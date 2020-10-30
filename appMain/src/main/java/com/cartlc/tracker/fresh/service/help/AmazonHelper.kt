/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package com.cartlc.tracker.fresh.service.help

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.event.EventRefreshProjects
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.ui.util.helper.BitmapResult
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by dug on 5/31/17.
 */

class AmazonHelper(
        private val db: DatabaseTable
) {

    companion object {
        private val TAG = AmazonHelper::class.simpleName


        //        private val BUCKET_NAME_DEVELOP = "fleetdev2"
        private const val BUCKET_NAME_RELEASE = "fleettlc"

        //        internal val IDENTITY_POOL_ID_DEVELOP = "us-east-2:38d2f2a2-9454-4472-9fec-9468f3700ba5"
        private const val IDENTITY_POOL_ID_RELEASE = "us-east-2:389282dd-de71-4849-a68b-2b126b3de5f3"
    }

    internal val BUCKET_NAME: String
    internal val IDENTITY_POOL_ID: String
    internal var mCred: CognitoCachingCredentialsProvider? = null
    internal lateinit var mClient: AmazonS3
    internal var mTrans: TransferUtility? = null

    init {
        BUCKET_NAME = BUCKET_NAME_RELEASE
        IDENTITY_POOL_ID = IDENTITY_POOL_ID_RELEASE
    }

    private fun init(context: Context) {
        if (mCred == null) {
            mCred = CognitoCachingCredentialsProvider(
                    context,
                    IDENTITY_POOL_ID,
                    Regions.US_EAST_2
            )
            mClient = AmazonS3Client(mCred)
        }
        if (mTrans == null) {
            mTrans = TransferUtility(mClient, context)
        }
    }

    fun cleanup() {
        mTrans = null
    }

    fun sendPictures(ctx: Context, list: List<DataEntry>): Boolean {
        var flag = false
        var count = 0
        for (entry in list) {
            if (sendPictures(ctx, entry) == 0) {
                if (entry.checkPictureUploadComplete()) {
                    flag = true
                }
            } else {
                count++
            }
        }
        if (DataEntry.UPLOAD_DEBUG) {
            error("UPLOAD DEBUG: after checking " + list.size + " entries, complete flag was $flag, with $count entries still working")
        }
        return flag
    }

    private fun sendPictures(ctx: Context, entry: DataEntry): Int {
        var countUploading = 0
        val fileNotFound = mutableListOf<String>()
        for (item in entry.pictures) {
            if (!item.uploaded) {
                when (val result = sendPicture(ctx, entry, item)) {
                    is BitmapResult.FILE_NOT_FOUND -> {
                        fileNotFound.add(result.filename)
                    }
                    BitmapResult.FILE_NAME_NULL -> {
                        error("UPLOAD null file found")
                    }
                    BitmapResult.MEDIA_NOT_MOUNTED -> {
                        error("UPLOAD media not mounted")
                    }
                    is BitmapResult.EXCEPTION -> {
                        error("UPLOAD exception ${result.message}")
                    }
                    BitmapResult.OK -> {
                        countUploading++
                    }
                }
            }
        }
        if (DataEntry.UPLOAD_DEBUG) {
            if (fileNotFound.isNotEmpty()) {
                val files = fileNotFound.joinToString(", ")
                error("UPLOAD missing files: $files")
            }
        }
        return countUploading
    }

    private fun sendPicture(ctx: Context, entry: DataEntry, item: DataPicture): BitmapResult {
        val result = item.buildScaledFile()
        if (result != BitmapResult.OK) {
            return result
        }
        val uploadingFile = item.scaledFile ?: return BitmapResult.FILE_NAME_NULL

        init(ctx)

        val key = item.unscaledFile.name
        val observer = mTrans?.upload(
                BUCKET_NAME, /* The bucket to upload to */
                key, /* The key for the uploaded object */
                uploadingFile /* The file where the data to upload existsUnscaled */
        )
        observer?.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    if (uploadComplete(entry, item)) {
                        EventBus.getDefault().post(EventRefreshProjects())
                    }
                } else if (DataEntry.UPLOAD_DEBUG) {
                    error("UPLOAD DEBUG: still working on picture with $state")
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}

            override fun onError(id: Int, ex: Exception) {
                TBApplication.ReportError(ex, AmazonHelper::class.java, "sendPicture()", "amazon")
            }
        })
        return BitmapResult.OK
    }

    @Synchronized
    private fun uploadComplete(entry: DataEntry, item: DataPicture): Boolean {
        db.tablePicture.setUploaded(item)
        return entry.checkPictureUploadComplete()
    }


    private fun msg(msg: String) {
        Timber.tag(TAG).i(msg)
    }

    private fun verbose(msg: String) {
        Timber.tag(TAG).d(msg)
    }

    private fun error(msg: String) {
        Timber.tag(TAG).e(msg)
    }

}
