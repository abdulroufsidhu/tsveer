package io.github.abdulroufsidhu.tasveer.data

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.abdulroufsidhu.tasveer.api.Api
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val api = Api()
    private var pageNumber = 0
    val query = mutableStateOf("")
    val isLoading = mutableStateOf(false)

    val photos = mutableStateListOf<ViewPhoto>()
    val currentIndex = mutableIntStateOf(-1)

    fun fetch(query: String) {
        this.query.value = query
        photos.clear()
        fetch()
    }

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            try {
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
                        ViewPhoto(it?.id ?: "", it?.urls?.thumb ?: "", it?.urls?.full ?: "")
                    }
                )
            } catch (e: ServerResponseException) {
                Log.w("Ktor", "Error getting remote items: ${e.response.status.description}", e)
            } catch (e: ClientRequestException) {
                Log.w("Ktor", "Error getting remote items: ${e.response.status.description}", e)
            } catch (e: RedirectResponseException) {
                Log.w("Ktor", "Error getting remote items: ${e.response.status.description}", e)
            } catch (e: Exception) {
                Log.w("Ktor", "Error getting remote items: ${e.message}", e)
            }
            isLoading.value = false
        }
    }
}