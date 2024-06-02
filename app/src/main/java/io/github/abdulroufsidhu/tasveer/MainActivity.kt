package io.github.abdulroufsidhu.tasveer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.abdulroufsidhu.tasveer.api.Api
import io.github.abdulroufsidhu.tasveer.data.ViewPhoto
import io.github.abdulroufsidhu.tasveer.ui.theme.TasveerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val api = Api()
    private var pageNumber = 0
    val query = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    val photos = mutableStateListOf<ViewPhoto>()

    fun fetch(query: String) {
        this.query.value = query
        photos.clear()
        fetch()
    }

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            pageNumber++
            photos.addAll(
                api.pexels(pageNumber, this@MainViewModel.query.value).data?.map {
                    ViewPhoto(
                        it?.id ?: "",
                        it?.attributes?.image?.small ?: "",
                        it?.attributes?.image?.download_link ?: "",
                    )
                } ?: emptyList()
            )
            photos.addAll(
                api.unsplash(pageNumber, this@MainViewModel.query.value).map {
                    ViewPhoto(it?.id ?: "", it?.urls?.small ?: "", it?.urls?.full ?: "",)
                }
            )
            isLoading.value = false
        }
    }
}

class MainActivity : ComponentActivity() {
    private val vm by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.fetch()
        enableEdgeToEdge()
        setContent {
            TasveerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        io.github.abdulroufsidhu.tasveer.ui.screens.List(vm = vm)
                    }
                }
            }
        }
    }
}