package com.carlosjimz87.installertutorial

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
        val APKS = listOf(
            "https://spotdyna-app.s3.eu-west-1.amazonaws.com/apk/copyApp.apk",
            "https://spotdyna-app.s3.eu-west-1.amazonaws.com/apk/copyApp_signed.apk"
        )
    }

    private lateinit var downloadController: DownloadController
    private val installManager: InstallManager = InstallManager(this, DownloadMethod.PROVIDER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadController = DownloadController(this, APKS[0])

        downloadController.downloadState.observe(this){ download ->
            Timber.d("Download completed $download")
            installManager.install(download)
        }

        checkStoragePermission()

    }

    private fun start() {
        // start downloading
        Timber.d("Starting download")
        downloadController.enqueueDownload()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start()
            } else {
                // Permission request was denied.
                mainLayout.showSnackbar(
                    getString(R.string.storage_permission_denied),
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }


    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("Permission granted")
            // start downloading
            downloadController.enqueueDownload()
        } else {
            Timber.w("Permission required")
            // Permission is missing and must be requested.
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {

        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mainLayout.showSnackbar(
                getString(R.string.storage_access_required),
                Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }

        } else {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }
}