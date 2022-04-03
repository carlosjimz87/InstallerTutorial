package com.carlosjimz87.installertutorial.managers

import android.content.Context
import android.content.Intent
import android.os.Build
import com.carlosjimz87.installertutorial.models.MDownload
import com.carlosjimz87.installertutorial.utils.Constants.REPLACE_ACTION
import timber.log.Timber

enum class InstallMethod {
    PROVIDER,
    INTENT,
    COMMANDS,
    REPLACE_PACKAGE,
}

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
            InstallMethod.REPLACE_PACKAGE -> {
                installViaReplacePackage(download)
            }
            else -> {}
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

    private fun installViaIntent(download: MDownload, intent: Intent? = null) {
        execIntent(
            intent = intent ?: Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                setDataAndType(
                    download.uri,
                    download.mimeType
                )
            }
        )
    }

    private fun installViaCommands(download: MDownload) {
        runOnExecutor(
            INSTALL_COMMANDS + download.uri.path,
            onResponse = {
                Timber.d("App ${download.filename} installed")
            },
            onError = { error ->
                Timber.e("Error installing ${error.message}")
            })
    }

    private fun installViaReplacePackage(download: MDownload) {
        runOnExecutor(
            "$REPLACE_PACKAGE_INTENT_COMMANDS ${download.filename}$UPDATE_RECEIVER",
            onResponse = {
                Timber.d("App ${download.filename} installed")
            },
            onError = { error ->
                Timber.e("Error installing ${error.message}")
            })
    }

    private fun runOnExecutor(
        commands: String,
        onResponse: ((String) -> Unit),
        onError: ((Throwable) -> Unit)
    ) {
        RuntimeExecutor.execute(commands)
            .unsafeRunAsync {
                it.fold(
                    { error ->
                        onError(error)
                    },
                    { result ->
                        onResponse("$result")
                    }
                )
            }
    }

    companion object {
        const val INSTALL_COMMANDS = "pm install -r -d "
        const val UPDATE_RECEIVER = "/.UpdateReceiver"
        const val REPLACE_PACKAGE_INTENT_COMMANDS = "am broadcast -a  $REPLACE_ACTION -n "
    }
}