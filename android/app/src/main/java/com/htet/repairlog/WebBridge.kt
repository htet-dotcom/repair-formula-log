package com.htet.repairlog

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import java.io.File

/** Exposed to the page as window.RFLNative. Methods run on a WebView background thread. */
class WebBridge(private val activity: MainActivity) {

    /** Saves a photo into Pictures/RepairLog. Returns "ok" or "error". */
    @JavascriptInterface
    fun saveImage(name: String, mime: String, base64: String): String = try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val safeName = name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, safeName)
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RepairLog")
            }
            val resolver = activity.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("insert failed")
            resolver.openOutputStream(uri).use { it!!.write(bytes) }
        } else {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.runOnUiThread { activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1) }
                error("permission")
            }
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "RepairLog")
            dir.mkdirs()
            File(dir, safeName).writeBytes(bytes)
        }
        "ok"
    } catch (e: Exception) {
        "error"
    }

    /** Asks for a Drive token; the answer arrives in window.rflDriveToken(token, seconds, error). */
    @JavascriptInterface
    fun driveToken(interactive: Boolean) {
        activity.runOnUiThread { activity.requestDriveToken(interactive) }
    }
}
