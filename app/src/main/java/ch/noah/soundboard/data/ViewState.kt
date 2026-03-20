package ch.noah.soundboard.data

sealed class ViewState<out T> {
	data object Loading : ViewState<Nothing>()
	data class SilentLoading<T>(val data: T) : ViewState<T>()
	data class Success<T>(val data: T) : ViewState<T>()
	data class Error(val message: String) : ViewState<Nothing>()
}
