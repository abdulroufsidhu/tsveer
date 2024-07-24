package io.github.abdulroufsidhu.tasveer.operations

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import io.github.abdulroufsidhu.tasveer.findActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

private const val TAG = "wallpaper-applier"

suspend fun Context.applyWallpaper(imageUrl: String, onDownloadComplete: (Boolean)->Unit) {
    withContext(Dispatchers.Main) {
        val imageUri = downloadImageToCache(this@applyWallpaper, imageUrl)
        onDownloadComplete(true)
        Log.i(TAG, "applyWallpaper: imageUri-> ${imageUri}")
        if (imageUri != null ) {
            withContext(Dispatchers.Main) {
                val wallpaperManager = WallpaperManager.getInstance(this@applyWallpaper)
                val cropAndSetWallpaperIntent = wallpaperManager.getCropAndSetWallpaperIntent(imageUri)
                cropAndSetWallpaperIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                this@applyWallpaper.startActivity(cropAndSetWallpaperIntent)
            }
        } else {
            onDownloadComplete(true)
            findActivity()?.let {
                Toast.makeText(it, "Failed to download image", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

private suspend fun downloadImageToCache(context: Context, imageUrl: String): Uri? = withContext(Dispatchers.IO){
    try {
        val url = URL(imageUrl)
        val connection = url.openConnection()
        val inputStream = connection.getInputStream()

        val bitmap = BitmapFactory.decodeStream(inputStream)

        val fileName = "temp_wallpaper.jpg"
        val file = File(context.getExternalFilesDir("wallpaper"), fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
        FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.fileprovider",
            file,
        )
    } catch (e: Exception) {
        Log.w(TAG, "downloadImageToCache: ", e)
        null
    }
}
