package com.carlosjimz87.installertutorial.managers

import android.content.Context
import com.carlosjimz87.installertutorial.models.MDownload
import com.carlosjimz87.installertutorial.utils.*
import com.carlosjimz87.installertutorial.utils.Utils.sendInstalledIntent
import com.carlosjimz87.installertutorial.utils.Utils.str
import timber.log.Timber
import java.io.File

class InstallManager2(
    private val context: Context,
    private val rooted: Rooted = Utils.isRoot(),
    private val manufacturer: String = Utils.getManufacturer(),
) {

    enum class InstallVariant {
        PHILLIPS_INTENT,
        PLAYER_INTENT,
        RUNTIME
    }

    fun install(download:MDownload){
        this.process(
            download.uri.path.toString(),
            download.packageName ?: "",
            download.activityName ?: ""
        )
    }

    private fun process(
        path: String,
        packageName: String,
        activityName: String,
        isPlayer: Boolean? = false
    ) {
        val file = File(path)
        Timber.d("Env: rooted:$rooted | manufacturer:$manufacturer | player:$isPlayer")
        Timber.d("To install ${file.absolutePath} (e: ${file.exists()})")

        when {
            manufacturer == Manufacturers.HYBROAD && rooted -> {
                IntentInstaller(context, rooted).install(
                    InstallVariant.RUNTIME,
                    path,
                    packageName,
                    activityName
                )
            }
            manufacturer == Manufacturers.PHILLIPS -> {
                IntentInstaller(context, rooted).install(
                    InstallVariant.PHILLIPS_INTENT,
                    path,
                    packageName,
                    activityName
                )
            }
            isPlayer == true -> {
                IntentInstaller(context, rooted).install(
                    InstallVariant.PLAYER_INTENT,
                    path,
                    packageName,
                    activityName
                )
            }
            else -> {
                Timber.d("NONE")
            }
        }

    }

    class IntentInstaller(
        private val context: Context,
        private val rooted: Rooted
    ) {

        fun install(
            installVariant: InstallVariant,
            path: String,
            packageName: String,
            activityName: String
        ) {
            Timber.d("Installing via intent $installVariant on $path with pN:$packageName and aN:$activityName")
            when (installVariant) {

                InstallVariant.RUNTIME -> {
                    SysInstaller(context, rooted).apply {
                        this.install(
                            cmd = SysInstaller.INSTALL_CMD,
                            appPath = path,
                            appName = path.substringAfterLast("/")
                        )
                    }
                }
                InstallVariant.PHILLIPS_INTENT -> {
                    IntentInstaller(context, rooted).installOnPhillips(
                        path,
                        packageName,
                        activityName
                    )
                }
                InstallVariant.PLAYER_INTENT -> {
                    IntentInstaller(context, rooted).installPlayer(
                        path
                    )
                }
            }
        }

        private fun installOnPhillips(path: String, packageName: String, activityName: String) {
            SysInstaller(context, rooted).install(
                cmd = SysInstaller.BROADCAST_CMD + updateAppOnPhillipsRomArgs(
                    appPackageName = packageName,
                    activityName = activityName
                ),
                appPath = path,
                appName = path.substringAfterLast("/")
            )
        }

        private fun installPlayer(path: String) {
            SysInstaller(context, rooted).install(
                cmd = SysInstaller.BROADCAST_CMD + toUpdatePlayerArgs(),
                appPath = path,
                appName = path.substringAfterLast("/")
            )
        }


        private fun updateAppOnPhillipsRomArgs(
            appPackageName: String,
            activityName: String,
            keep: Boolean? = true,
            isAllowDowngrade: Boolean? = true
        ): String {
            return arrayOf(
                "-a", PhillipsActions.TPV_UPDATE,
                "--ez", "keep", "$keep",
                "--ez", "isAllowDowngrade", "$isAllowDowngrade",
                "--es", "packageName", appPackageName,
                "--es", "activityName", "$appPackageName.$activityName",
                "-f", "0x1000000",
                "-es", "filePath",
            ).joinToString(" ")
        }

        private fun toUpdatePlayerArgs(): String {
            return arrayOf(
                "-a", Actions.PACKAGE_REPLACED,
                "-n", "${AppNames.PLAYER}/.UpdateReceiver"
            ).joinToString(" ")
        }

    }


    class SysInstaller(
        private val context: Context,
        private val rooted: Rooted
    ) {

        companion object {

            const val DELETE_CMD = "&& rm -rf "
            const val INSTALL_CMD = "pm install -r "
            const val BROADCAST_CMD = "am broadcast "
        }

        fun install(cmd: String, appPath: String, appName:String,  finalDelete: Boolean = false) {

            val finalCMD = "$cmd $appPath ${deleteAfter(appPath, finalDelete)}"
            Timber.d("Installing via system -> $finalCMD")

            RuntimeExecutor.execute(finalCMD)
                .unsafeRunAsync {
                    it.fold(
                        { error ->
                            Timber.e(" Installation error ${error.message}")
                            sendInstalledIntent(context, appName, false)
                        },
                        { result ->
                            when(result){
                                is RuntimeExecutor.RuntimeResult.Success ->{
                                    Timber.d(" Installation Successful ${result.output}")
                                    sendInstalledIntent(context, appName, true)
                                }
                                is RuntimeExecutor.RuntimeResult.Failure ->{
                                    Timber.e(" Installation error ${result.error}")
                                    sendInstalledIntent(context, appName, false)
                                }
                            }
                        }
                    )
                }
        }

        private fun deleteAfter(path: String?, should: Boolean): String {
            return if (should && path?.isNotEmpty() == true) "$DELETE_CMD $path" else ""
        }
    }
}