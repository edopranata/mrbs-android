package com.ropekanbaru.booking.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ropekanbaru.booking.data.remote.ApiErrors
import com.ropekanbaru.booking.data.remote.PageResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Memuat satu data dari API untuk sebuah layar: status memuat, tarik-untuk-memperbarui, dan error.
 * Data lama tetap tampil saat dimuat ulang.
 */
@Stable
class Loader<T>(private val scope: CoroutineScope, private val fetch: suspend () -> T) {
    var data by mutableStateOf<T?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var job: Job? = null

    fun load(refresh: Boolean = false) {
        job?.cancel()
        loading = data == null
        refreshing = refresh
        error = null
        job = scope.launch {
            try {
                data = fetch()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = ApiErrors.message(e)
            }
            loading = false
            refreshing = false
        }
    }

    /** Ganti data setempat setelah aksi berhasil (tanpa memuat ulang). */
    fun set(value: T) {
        data = value
    }
}

@Composable
fun <T> rememberLoader(vararg keys: Any?, fetch: suspend () -> T): Loader<T> {
    val scope = rememberCoroutineScope()
    return remember(*keys) { Loader(scope, fetch).also { it.load() } }
}

/** Daftar berhalaman (paginasi Laravel) dengan tombol/gulir "muat lebih banyak". */
@Stable
class PagedLoader<T>(private val scope: CoroutineScope, private val fetch: suspend (page: Int) -> PageResponse<T>) {
    var items by mutableStateOf<List<T>>(emptyList())
        private set
    var total by mutableStateOf(0)
        private set
    var loaded by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var page = 0
    private var lastPage = 1
    private var job: Job? = null

    val hasMore: Boolean get() = loaded && page < lastPage

    fun reload(refresh: Boolean = false) {
        job?.cancel()
        loading = !loaded
        refreshing = refresh
        loadingMore = false
        error = null
        job = scope.launch {
            try {
                val response = fetch(1)
                items = response.data
                total = response.meta.total
                page = response.meta.currentPage
                lastPage = response.meta.lastPage
                loaded = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = ApiErrors.message(e)
            }
            loading = false
            refreshing = false
        }
    }

    fun loadMore() {
        if (!hasMore || loadingMore || loading) return
        loadingMore = true
        job = scope.launch {
            try {
                val response = fetch(page + 1)
                items = items + response.data
                total = response.meta.total
                page = response.meta.currentPage
                lastPage = response.meta.lastPage
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = ApiErrors.message(e)
            }
            loadingMore = false
        }
    }
}

@Composable
fun <T> rememberPagedLoader(vararg keys: Any?, fetch: suspend (page: Int) -> PageResponse<T>): PagedLoader<T> {
    val scope = rememberCoroutineScope()
    return remember(*keys) { PagedLoader(scope, fetch).also { it.reload() } }
}
