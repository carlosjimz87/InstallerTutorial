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
import com.carlosjimz87.installertutorial.Constants.FILE_BASE_PATH
import com.carlosjimz87.installertutorial.Constants.FILE_NAME
import com.carlosjimz87.installertutorial.Constants.MIME_TYPE
import com.carlosjimz87.installertutorial.Constants.PROVIDER_PATH
import timber.log.Timber
import java.io.File

enum class InstallMethod {
    PROVIDER,
    INTENT
}

class DownloadController(private val context: Context, private val url: String) {


    private var downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private var prevId = -1L
    private var prevUri: Uri? = null

    fun enqueueDownload() {

        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        prevUri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()

        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle(context.resources.getString(R.string.title_file_download))
        request.setDescription(context.resources.getString(R.string.downloading))

        // set destination
        request.setDestinationUri(prevUri)
        Timber.d("Downloading ${file.name} from $prevUri to $destination")
        showInstallOption(destination)
        // Enqueue a new download and same the referenceId
        prevId = downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.downloading), Toast.LENGTH_LONG)
            .show()


    }

    private fun showInstallOption(
        destination: String
    ) {

        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                Timber.d("Download completed ${intent.extras.toString()}")
                val newDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if (newDownloadId == -1L) return

                val newUri: Uri?
                val method = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    newUri = getInstallUri(InstallMethod.PROVIDER, destination = destination)
                    InstallMethod.PROVIDER
                } else {
                    newUri = getInstallUri(InstallMethod.INTENT, newDownloadId = prevId)
                    InstallMethod.INTENT
                }

                if (checkDownloadedFile(prevUri)) {
                    install(newUri, method)
                }

                Timber.d("Unregister receiver")
                context.unregisterReceiver(this)
            }


        }
        context.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }


    fun checkDownloadedFile(uri: Uri?): Boolean {
        Timber.d("Verify file at ${uri?.path}")
        return uri?.path?.let { path ->
            File(path).run {
                this.exists() && this.isFile && this.canRead()
            }
        } ?: false
    }

    fun getInstallUri(
        method: InstallMethod,
        newDownloadId: Long? = null,
        destination: String? = null
    ): Uri? {
        return when (method) {
            InstallMethod.PROVIDER -> {
                destination?.let {
                    FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )
                }
            }
            InstallMethod.INTENT -> {
                newDownloadId?.let {
                    downloadManager.getUriForDownloadedFile(newDownloadId)
                }
            }
        }
    }

    fun install(uri: Uri?, method: InstallMethod) {
        Timber.d("Proceed to install on $${Build.VERSION.CODENAME} via $method")
        val intent: Intent = when (method) {
            InstallMethod.PROVIDER -> {
                Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    data = uri
                }
            }
            InstallMethod.INTENT -> {
                val mime = downloadManager.getMimeTypeForDownloadedFile(prevId)
                Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    setDataAndType(
                        uri,
                        mime
                    )
                }
            }
        }
        Timber.d("Installing $intent")
        context.startActivity(intent)
    }

}

