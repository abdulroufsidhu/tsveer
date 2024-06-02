package io.github.abdulroufsidhu.tasveer.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.ColorSpace
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.abdulroufsidhu.tasveer.R
import io.github.abdulroufsidhu.tasveer.data.ViewPhoto
import io.github.abdulroufsidhu.tasveer.download
import io.github.abdulroufsidhu.tasveer.findActivity
import io.github.abdulroufsidhu.tasveer.operations.applyWallpaper
import io.github.abdulroufsidhu.tasveer.ui.theme.TasveerTheme
import kotlinx.coroutines.launch

class ViewActivity : ComponentActivity() {

    companion object {
        private const val KEY_PHOTO: String = "photo"
        fun start(context: Context, photo: ViewPhoto) =
            context.startActivity(
                Intent(context, ViewActivity::class.java).putExtra(
                    KEY_PHOTO,
                    photo
                )
            )

        private fun getPhoto(intent: Intent) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra<ViewPhoto>(KEY_PHOTO, ViewPhoto::class.java)
            } else {
                intent.getParcelableExtra<ViewPhoto>(KEY_PHOTO)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val photo = getPhoto(intent)
        if (photo == null) {
            finish()
            return
        }
        setContent {
            TasveerTheme {
                View(photo)
            }
        }
    }
}

@Composable
private fun View(viewPhoto: ViewPhoto) {
    val context = LocalContext.current
    val scale = remember { mutableFloatStateOf(1f) }
    val pan = remember {
        mutableStateOf(Offset.Zero)
    }
    Box(
        modifier = Modifier
            .fillMaxSize(1f),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(viewPhoto.fullUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .error(R.drawable.ic_broken_image)
                .placeholder(R.drawable.image_loader)
                .build(),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectTransformGestures { _, panValue, zoom, _ ->
                        scale.floatValue *= zoom
                        pan.value = Offset(pan.value.x * panValue.x, pan.value.y * panValue.y)
                    }
                }
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale.floatValue)),
                    scaleY = maxOf(.5f, minOf(3f, scale.floatValue)),
                    translationX = pan.value.x,
                    translationY = pan.value.y,
                ),
            contentScale = ContentScale.Fit,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier) {
                val downloading = remember { mutableStateOf(false) }
                IconButton(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.CenterEnd),
                    onClick = {
                        context.findActivity()?.lifecycleScope?.launch {
                            downloading.value = true
                            context.applyWallpaper(viewPhoto.fullUrl) { downloading.value = !it }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "download button",
                    )
                }
                if (downloading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.CenterEnd),
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
            IconButton(
                onClick = { context.download(viewPhoto) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download),
                    contentDescription = "download button",
                )
            }
        }

    }
}