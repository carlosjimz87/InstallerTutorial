package com.carlosjimz87.installertutorial

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File



class InstallManager(
    private val context: Context,
    private val method: DownloadMethod
) {
    private fun createProviderIntent(download: MDownload): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            data = download.uri
        }
    }

    private fun createIntent(download: MDownload): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            setDataAndType(
                download.uri,
                download.mimeType
            )
        }
    }



    fun install(download: MDownload) {
        Timber.d("Proceed to install on SDK->${Build.VERSION.SDK_INT} via-> $method")
        val intent: Intent = when (method) {
            DownloadMethod.PROVIDER -> {
                createProviderIntent(download)
            }
            DownloadMethod.INTENT -> {
//                val mime = downloadManager.getMimeTypeForDownloadedFile(prevId)
                createIntent(download)
            }
        }
        Timber.d("Installing $intent")
//        context.startActivity(intent)
    }

}