package com.carlosjimz87.installertutorial.utils

import android.net.Uri
import timber.log.Timber
import java.io.File

object Utils {

    fun checkFileExist(uri: Uri? = null): Boolean {

        Timber.d("Verify file at ${uri?.path}")
        return uri?.path?.let { path ->
            val file = File(path)
            file.exists() && file.isFile && file.canRead()
        } ?: false
    }
}