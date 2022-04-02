package com.carlosjimz87.installertutorial.models

import android.net.Uri

data class MDownload(
    val uri: Uri,
    val filename: String?="",
    val mimeType: String?=""
)