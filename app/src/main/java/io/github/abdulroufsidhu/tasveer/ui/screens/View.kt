package io.github.abdulroufsidhu.tasveer.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.github.abdulroufsidhu.tasveer.R
import io.github.abdulroufsidhu.tasveer.data.MainViewModel
import io.github.abdulroufsidhu.tasveer.data.ViewPhoto
import io.github.abdulroufsidhu.tasveer.download
import io.github.abdulroufsidhu.tasveer.findActivity
import io.github.abdulroufsidhu.tasveer.operations.applyWallpaper
import io.github.abdulroufsidhu.tasveer.ui.theme.TasveerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ViewActivity"

class ViewActivity : ComponentActivity() {

    private val vm by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    companion object {
        private const val KEY_PHOTO: String = "photo"
        private const val KEY_PHOTO_LIST: String = "photo_list"
        private const val KEY_CURRENT_INDEX: String = "current_index"
        fun start(context: Context, photo: ViewPhoto) = start(context, arrayOf(photo), 0)

        fun start(context: Context, list: Array<ViewPhoto>, currentIndex: Int) =
            context.startActivity(
                Intent(context, ViewActivity::class.java).putParcelableArrayListExtra(
                    KEY_PHOTO_LIST, arrayListOf(*list)
                ).putExtra(KEY_CURRENT_INDEX, currentIndex)
            )

        private fun getPhoto(intent: Intent) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra<ViewPhoto>(KEY_PHOTO, ViewPhoto::class.java)
            } else {
                intent.getParcelableExtra<ViewPhoto>(KEY_PHOTO)
            }
    }

    private suspend fun initiateData() {
        vm.photos.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra<ViewPhoto>(KEY_PHOTO_LIST, ViewPhoto::class.java)
                ?.let { list ->
                    vm.photos.addAll(list.toTypedArray())
                }
        } else {
            intent.getParcelableArrayListExtra<ViewPhoto>(KEY_PHOTO_LIST)?.let { list ->
                vm.photos.addAll(list.toTypedArray())
            }
        }
        intent.getIntExtra(KEY_CURRENT_INDEX, -1).let {
            vm.currentIndex.intValue = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            initiateData()
            if (vm.currentIndex.intValue == -1) {
                withContext(Dispatchers.Main) { finish() }
                return@launch
            }
            withContext(Dispatchers.Main) {
                setContent {
                    TasveerTheme {
                        View(vm)
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
private fun View(vm: MainViewModel) {
    val photos = vm.photos
    val context = LocalContext.current
    val scale = remember { mutableFloatStateOf(1f) }
    val pan = remember {
        mutableStateOf(Offset.Zero)
    }
    val pagerState = rememberPagerState(
        initialPage = vm.currentIndex.intValue,
    ) {
        photos.size
    }
    Box(
        modifier = Modifier
            .fillMaxSize(1f),
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(1f).background(color = MaterialTheme.colorScheme.surface),
            state = pagerState, userScrollEnabled = true,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LaunchedEffect(key1 = pagerState.currentPage) {
                vm.currentIndex.intValue = pagerState.currentPage
                Log.d(TAG, "Current Index: ${vm.currentIndex.intValue}")
            }
            GlideImage(
                model = photos[it].fullUrl,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = maxOf(.5f, minOf(3f, scale.floatValue)),
                        scaleY = maxOf(.5f, minOf(3f, scale.floatValue)),
                        translationX = pan.value.x,
                        translationY = pan.value.y,
                    ),
                contentScale = ContentScale.Fit,
                loading = placeholder(ColorPainter(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            ) {
                it.thumbnail().diskCacheStrategy(DiskCacheStrategy.ALL)
            }
        }
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
                            context.applyWallpaper(photos[vm.currentIndex.intValue].fullUrl) {
                                downloading.value = !it
                            }
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
                onClick = {
                    Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                    context.download(photos[vm.currentIndex.intValue])
                },
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