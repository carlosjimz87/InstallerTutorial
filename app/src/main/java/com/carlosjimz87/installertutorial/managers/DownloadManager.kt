package com.carlosjimz87.installertutorial.managers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.carlosjimz87.installertutorial.R
import com.carlosjimz87.installertutorial.models.MDownload
import com.carlosjimz87.installertutorial.utils.Constants.FILE_BASE_PATH
import com.carlosjimz87.installertutorial.utils.Constants.FILE_NAME
import com.carlosjimz87.installertutorial.utils.Constants.MIME_TYPE
import com.carlosjimz87.installertutorial.utils.Constants.PROVIDER_PATH
import com.carlosjimz87.installertutorial.utils.Utils
import com.carlosjimz87.installertutorial.utils.getDestinationPath
import timber.log.Timber
import java.io.File

enum class InstallMethod {
    PROVIDER,
    INTENT,
    COMMANDS,
}

class DownloadController(
    private val context: Context,
    private val remoteUrl: String
) {

    private lateinit var uri: Uri
    private lateinit var downloadManager: DownloadManager
    private var prevId: Long = -1
    val downloadState: MutableLiveData<MDownload> by lazy {
        MutableLiveData<MDownload>()
    }

//    private fun testFilesCreation() {
//        val destination1 = context.getDestinationPath("getExternalFilesDir")
//        val destination2 = context.getDestinationPath("getExternalStorageDirectory")
//        val destination3 = context.getDestinationPath("getDownloadCacheDirectory")
//        val destination4 = context.getDestinationPath("getDataDirectory")
//
//
//        val file1 = createFileDestination(destination1)
//        val file2 = createFileDestination(destination2)
//        val file3 = createFileDestination(destination3)
//        val file4 = createFileDestination(destination4)
//
//        val a = 2
//    }

    private fun createFileDestination(destination: String): File? {

        val file = File(destination)
        val existF = file.exists()
        val isFileF = file.isFile
        val canRead = file.canRead()
        Timber.d("File in $destination exist:$existF isFile: $isFileF canRead:$canRead")
        return if (file.exists() && file.isFile && file.canRead())
            file
        else
            null
    }


    fun enqueueDownload() {
        val destination = context.getDestinationPath("getExternalFilesDir")

        uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists()) file.delete()

        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(remoteUrl)

        val request = DownloadManager.Request(downloadUri).apply {
            setMimeType(MIME_TYPE)
            setTitle(context.resources.getString(R.string.title_file_download))
            setDescription(context.resources.getString(R.string.downloading))
            setDestinationUri(uri)
        }

        Timber.d("Downloading $FILE_NAME from $uri to $destination")

        prepareDownloadReceiver()

        // Enqueue a new download and same the referenceId
        prevId = downloadManager.enqueue(request)
        Timber.d("Downloading...")
    }


    private fun prepareDownloadReceiver() {
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                Timber.d("Download done receiver ${intent.extras.toString()}")
                val newDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if (newDownloadId == -1L) return

                // create Download instance
                val download = MDownload(
                    uri,
                    FILE_NAME,
                    MIME_TYPE
                )

                if (Utils.checkFileExist(download.uri)) {
                    val newUri: Uri? = conformUriByMethod(download.uri)
                    Timber.d("DownloadState will change")
                    downloadState.postValue(
                        download.copy(
                            uri = newUri ?: download.uri,
                            filename = download.filename,
                            mimeType = downloadManager.getMimeTypeForDownloadedFile(prevId)
                        )
                    )
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

    private fun conformUriByMethod(destUri: Uri?): Uri? {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createInstallUri(InstallMethod.PROVIDER, destination = destUri?.path)
        } else {
            createInstallUri(InstallMethod.INTENT, newDownloadId = prevId)
        }
    }

    private fun createInstallUri(
        method: InstallMethod,
        newDownloadId: Long? = null,
        destination: String? = null
    ): Uri? {
        return when (method) {
            InstallMethod.PROVIDER -> {
                destination?.let {
                    FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + PROVIDER_PATH,
                        File(destination)
                    )
                }
            }
            InstallMethod.INTENT -> {
                newDownloadId?.let {
                    downloadManager.getUriForDownloadedFile(newDownloadId)
                }
            }
            else -> null
        }
    }


}

