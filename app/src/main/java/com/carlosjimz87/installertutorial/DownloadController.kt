package com.carlosjimz87.installertutorial

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.carlosjimz87.installertutorial.Constants.FILE_BASE_PATH
import com.carlosjimz87.installertutorial.Constants.FILE_NAME
import com.carlosjimz87.installertutorial.Constants.MIME_TYPE
import com.carlosjimz87.installertutorial.Constants.PROVIDER_PATH
import timber.log.Timber
import java.io.File

enum class DownloadMethod {
    PROVIDER,
    INTENT
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

    private fun testFilesCreation() {
        val destination1 = getDestinationPath("getExternalFilesDir")
        val destination2 = getDestinationPath("getExternalStorageDirectory")
        val destination3 = getDestinationPath("getDownloadCacheDirectory")
        val destination4 = getDestinationPath("getDataDirectory")


        val file1 = createFileDestination(destination1)
        val file2 = createFileDestination(destination2)
        val file3 = createFileDestination(destination3)
        val file4 = createFileDestination(destination4)

        val a = 2

    }

    private fun getDestinationPath(destinationMethod: String): String {
        val destination = when (destinationMethod) {
            "getExternalFilesDir" -> {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + FILE_NAME
            }
            "getExternalStorageDirectory" -> {
                Environment.getExternalStorageDirectory().toString() + File.separator + FILE_NAME
            }
            "getDownloadCacheDirectory" -> {
                Environment.getDownloadCacheDirectory().toString() + File.separator + FILE_NAME
            }
            "getDataDirectory" -> {
                Environment.getDataDirectory().toString() + File.separator + FILE_NAME
            }
            else -> {
                ""
            }
        }
        return destination
    }

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
        val destination = getDestinationPath("getExternalFilesDir")

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
                    prevId.toString(),
                    FILE_NAME,
                    MIME_TYPE
                )

                if (Utils.checkFileExist(download.uri)) {
                    val newUri: Uri? = conformUriByMethod(download.uri)
                    Timber.d("DownloadState will change")
                    downloadState.postValue(
                        download.copy(
                            id = newDownloadId.toString(),
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
            createInstallUri(DownloadMethod.PROVIDER, destination = destUri?.path)
        } else {
            createInstallUri(DownloadMethod.INTENT, newDownloadId = prevId)
        }
    }

    private fun createInstallUri(
        method: DownloadMethod,
        newDownloadId: Long? = null,
        destination: String? = null
    ): Uri? {
        return when (method) {
            DownloadMethod.PROVIDER -> {
                destination?.let {
                    FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + PROVIDER_PATH,
                        File(destination)
                    )
                }
            }
            DownloadMethod.INTENT -> {
                newDownloadId?.let {
                    downloadManager.getUriForDownloadedFile(newDownloadId)
                }
            }
        }
    }


}

