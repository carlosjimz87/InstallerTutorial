package com.carlosjimz87.installertutorial.utils

object Constants {

    const val FILE_NAME = "copyfiles.apk"
    const val FILE_BASE_PATH = "file://"
    const val MIME_TYPE = "application/vnd.android.package-archive"
    const val PROVIDER_PATH = ".provider"
    const val ACTIVITY = "MainActivity"
    const val COPYFILES = "com.carlosjimz87.copyfiles"

}

object Actions{
    const val PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED"
    const val INSTALLED = "com.onthespot.action.INSTALLED"
}

object AppNames{
    const val PLAYER = "com.onthespot.androidplayer"
}

object Manufacturers{
    const val PHILLIPS = "TPV"
    const val HYBROAD = "Hisilicon"
}

object PhillipsActions{
    const val TPV_REBOOT_DEVICE = "php.intent.action.REBOOT"
    const val TPV_TAKE_SCREENSHOT = "php.intent.action.TAKE_SCREENSHOT"
    const val TPV_UPDATE = "php.intent.action.UPDATE_APK"
}