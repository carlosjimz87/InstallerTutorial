package com.carlosjimz87.installertutorial.utils

import android.content.Context
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.io.File

fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(
    permissionsArray: Array<String>,
    requestCode: Int
) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}

fun Context.getDestinationPath(destinationMethod: String): String {
    val destination = when (destinationMethod) {
        "getExternalFilesDir" -> {
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + File.separator + Constants.FILE_NAME
        }
        "getExternalStorageDirectory" -> {
            Environment.getExternalStorageDirectory()
                .toString() + File.separator + Constants.FILE_NAME
        }
        "getDownloadCacheDirectory" -> {
            Environment.getDownloadCacheDirectory()
                .toString() + File.separator + Constants.FILE_NAME
        }
        "getDataDirectory" -> {
            Environment.getDataDirectory().toString() + File.separator + Constants.FILE_NAME
        }
        else -> {
            ""
        }
    }
    return destination
}

fun View.showSnackbar(msgId: Int, length: Int) {
    showSnackbar(context.getString(msgId), length)
}

fun View.showSnackbar(msg: String, length: Int) {
    showSnackbar(msg, length, null, {})
}

fun View.showSnackbar(
    msgId: Int,
    length: Int,
    actionMessageId: Int,
    action: (View) -> Unit
) {
    showSnackbar(context.getString(msgId), length, context.getString(actionMessageId), action)
}

fun View.showSnackbar(
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(this, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    }
}
