package io.github.abdulroufsidhu.tasveer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.github.abdulroufsidhu.tasveer.data.MainViewModel
import io.github.abdulroufsidhu.tasveer.download
import io.github.abdulroufsidhu.tasveer.scrollEndCallback

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun List(vm: MainViewModel) {
    val photos = vm.photos
    val context = LocalContext.current
    val listState = rememberLazyStaggeredGridState()
    val loading = vm.isLoading
    Column(
        modifier = androidx.compose.ui.Modifier
            .padding(start = 16.dp, top = 16.dp),
    ) {
        if (loading.value) {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                strokeCap = StrokeCap.Round,
            )
        }
        LazyVerticalStaggeredGrid(
            state = listState.apply {
                scrollEndCallback {
                    vm.fetch()
                }
            },
            columns = StaggeredGridCells.Adaptive(150.dp)
        ) {

            item(span = StaggeredGridItemSpan.FullLine) {
                OutlinedTextField(
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                    value = vm.query.value,
                    onValueChange = vm::fetch,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "search field icon"
                        )
                    },
                    maxLines = 1,
                    singleLine = true,
                )
            }
            itemsIndexed(photos) { index, viewPhoto ->
                ElevatedCard(
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                        .combinedClickable(
                            onClick = {
                                ViewActivity.start(context, vm.photos.toTypedArray(), index)
                            },
                            onLongClick = {
                                Toast
                                    .makeText(context, "Downloading", Toast.LENGTH_SHORT)
                                    .show()
                                context.download(viewPhoto)
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(),
                ) {
                    GlideImage(
                        model = viewPhoto.thumbnailUrl,
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        it.diskCacheStrategy(DiskCacheStrategy.ALL)
                    }
                }
            }
        }
    }
}