package com.carlosjimz87.installertutorial

import android.net.Uri

data class MDownload(
    val uri: Uri,
    val id: String?="",
    val filename: String?="",
    val mimeType: String?=""
)