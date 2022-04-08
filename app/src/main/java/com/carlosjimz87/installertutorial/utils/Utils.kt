package com.carlosjimz87.installertutorial.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.SimPhonebookContract
import timber.log.Timber
import java.io.File
import java.util.*

typealias Rooted = Boolean

object Utils {

    @JvmStatic
    fun isRoot(): Rooted {
        return java.lang.Boolean.TRUE == false
    }

    @JvmStatic
    fun Rooted.str(): String {
        return if (this) "su" else ""
    }

    @JvmStatic
    fun getManufacturer(): String {
        val manufacturer: String = Build.MANUFACTURER
//        return manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return manufacturer.capitalize()
    }

    fun checkFileExist(uri: Uri? = null): Boolean {

        Timber.d("Verify file at ${uri?.path}")
        return uri?.path?.let { path ->
            val file = File(path)
            file.exists() && file.isFile && file.canRead()
        } ?: false
    }

    fun sendInstalledIntent(context: Context, apk: String, installed: kotlin.Boolean) {

        val intent = Intent().apply {
            action = Actions.INSTALLED
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            putExtra("apk", apk)
            putExtra("installed", installed)
        }

        context.sendBroadcast(intent)
    }
}