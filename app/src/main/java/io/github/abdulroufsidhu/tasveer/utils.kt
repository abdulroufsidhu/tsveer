package io.github.abdulroufsidhu.tasveer

import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.abdulroufsidhu.tasveer.data.ViewPhoto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

fun Context.download(viewPhoto: ViewPhoto) {
    val request = DownloadManager.Request(Uri.parse(viewPhoto.fullUrl))
        .setTitle(getString(R.string.app_name))
        .setDescription("Downloading Wallpaper ${viewPhoto.id}")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_PICTURES,
            "${getString(R.string.app_name)}/${viewPhoto.id}.jpg"
        )
    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> findActivity()
    else -> null
}

@Composable
inline fun LazyStaggeredGridState.scrollEndCallback(crossinline callback: () -> Unit) {
    LaunchedEffect(key1 = this) {
        snapshotFlow { layoutInfo }
            .filter { it.totalItemsCount > 0 }
            .map { it.totalItemsCount == (it.visibleItemsInfo.lastOrNull()?.index ?: -1).inc() }
            .distinctUntilChanged()
            .filter { it }
            .onEach { callback() }
            .collect()
    }
}