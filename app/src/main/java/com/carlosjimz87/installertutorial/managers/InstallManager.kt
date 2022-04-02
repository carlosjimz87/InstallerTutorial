package com.carlosjimz87.installertutorial.managers

import android.content.Context
import android.content.Intent
import android.os.Build
import com.carlosjimz87.installertutorial.models.MDownload
import timber.log.Timber

const val INSTALL_COMMANDS = "pm install -r -d "

class InstallManager(
    private val context: Context,
    private val method: InstallMethod? = InstallMethod.COMMANDS
) {
    fun install(download: MDownload) {
        Timber.d("Proceed to install on SDK->${Build.VERSION.SDK_INT} via-> $method")
        when (method) {
            InstallMethod.PROVIDER -> {
                installViaProvider(download)
            }
            InstallMethod.INTENT -> {
                installViaIntent(download)
            }
            InstallMethod.COMMANDS -> {
                installViaCommands(download)
            }
            else -> {}
        }
    }

    private fun installViaCommands(download: MDownload) {
        RuntimeExecutor.execute(INSTALL_COMMANDS + download.uri.path)
            .unsafeRunAsync {
                it.fold(
                    { error ->
                        Timber.e("Error installing ${error.message}")
                    },
                    {
                        Timber.d("App ${download.filename} installed")
                    }
                )
            }

    }


    private fun execIntent(intent: Intent) {
        context.startActivity(intent)
    }

    private fun installViaProvider(download: MDownload) {
        execIntent(
            intent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                data = download.uri
            })
    }

    private fun installViaIntent(download: MDownload) {
        execIntent(
            intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                setDataAndType(
                    download.uri,
                    download.mimeType
                )
            }
        )
    }

}