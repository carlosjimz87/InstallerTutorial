package com.carlosjimz87.installertutorial

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.carlosjimz87.installertutorial.Constants.APP_INSTALL_PATH
import com.carlosjimz87.installertutorial.Constants.FILE_BASE_PATH
import com.carlosjimz87.installertutorial.Constants.FILE_NAME
import com.carlosjimz87.installertutorial.Constants.MIME_TYPE
import com.carlosjimz87.installertutorial.Constants.PROVIDER_PATH
import timber.log.Timber
import java.io.File

class DownloadController(private val context: Context, private val url: String) {


    private var downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private var downloadID = -1L
    fun enqueueDownload() {

        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()

        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle(context.resources.getString(R.string.title_file_download))
        request.setDescription(context.resources.getString(R.string.downloading))

        // set destination
        request.setDestinationUri(uri)
        Timber.d("Downloading ${file.name} from $uri to $destination")
        showInstallOption(destination, uri)
        // Enqueue a new download and same the referenceId
        downloadID = downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.downloading), Toast.LENGTH_LONG)
            .show()


    }

    private fun showInstallOption(
        destination: String,
        uri: Uri
    ) {

        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                if (downloadID == -1L) return

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                } else {
                    Timber.d("Lower apis")
                    val install = Intent(Intent.ACTION_VIEW)
                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                    val newDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    val newUri: Uri? = downloadManager.getUriForDownloadedFile(newDownloadId)

                    if (newDownloadId == downloadID && uri.path?.equals(newUri) == true) {
                        install.setDataAndType(
                            uri,
                            downloadManager.getMimeTypeForDownloadedFile(downloadID)
                        );

                        install.setDataAndType(
                            uri,
                            APP_INSTALL_PATH
                        )
                        context.startActivity(install)
                    }

                    context.unregisterReceiver(this)
                    // finish()
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}