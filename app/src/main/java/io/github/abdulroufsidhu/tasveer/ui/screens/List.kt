package io.github.abdulroufsidhu.tasveer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.github.abdulroufsidhu.tasveer.MainViewModel
import io.github.abdulroufsidhu.tasveer.R
import io.github.abdulroufsidhu.tasveer.download
import io.github.abdulroufsidhu.tasveer.scrollEndCallback

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
            items(photos) { viewPhoto ->
                ElevatedCard(
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                        .clickable {
                            ViewActivity.start(context, viewPhoto)
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(),
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
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            context.download(viewPhoto)
                        },
                        shape = RectangleShape
                    ) {
                        Text(text = "Download")
                    }
                }
            }
        }
    }
}