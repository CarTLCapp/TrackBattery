package com.cartlc.tracker.fresh.service.update

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.cartlc.tracker.BuildConfig
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import com.cartlc.tracker.fresh.ui.common.DeviceHelper
import java.io.File
import java.util.*


class UpdateAppUseCaseImpl(
        private val context: Activity,
        private val messageHandler: MessageHandler,
        private val prefHelper: PrefHelper,
        private val deviceHelper: DeviceHelper
) : UpdateAppUseCase {

    companion object {
        private const val DEBUG_DOWNLOAD = true
        private const val UPDATE_URL = "https://www.dropbox.com/s/flcs0t99g1as4a7/CarTLC-r4.03.apk?dl=0"
        private const val DESTINATION_NAME = "CarTLC-latest.apk"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val FILE_PROVIDER = ".fileprovider"
    }

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    // region UpdateAppUseCase

    override val isUpdateAvailable: Boolean
        get() = deviceHelper.versionInt < prefHelper.latestKnownVersion || DEBUG_DOWNLOAD

    override fun updateApp(requestCode: Int) {
        val destinationString: String = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + DESTINATION_NAME
        val destinationFile = File(destinationString)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }
        val destinationUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            Uri.fromFile(destinationFile)
        else
            FileProvider.getUriForFile(Objects.requireNonNull(context), BuildConfig.APPLICATION_ID + FILE_PROVIDER, destinationFile)

        val sourceUrl: String = UPDATE_URL
        val request = DownloadManager.Request(Uri.parse(sourceUrl))
        request.setDescription(messageHandler.getString(StringMessage.title_update))
        request.setTitle(messageHandler.getString(StringMessage.app_name))
        request.setDestinationUri(destinationUri)
        request.setMimeType(MIME_TYPE)

        val downloadId = downloadManager.enqueue(request)

        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val install = Intent(Intent.ACTION_VIEW)
                install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                install.setDataAndType(destinationUri, downloadManager.getMimeTypeForDownloadedFile(downloadId))
                context.startActivityForResult(install, requestCode)
                context.unregisterReceiver(this)
                context.finish()
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        downloadManager.enqueue(request)
    }

    // endregion UpdateAppUseCase

}