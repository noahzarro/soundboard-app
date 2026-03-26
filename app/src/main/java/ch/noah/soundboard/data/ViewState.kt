package ch.noah.soundboard.data

sealed class ViewState<out T> {
	data object Loading : ViewState<Nothing>()
	data class SilentLoading<T>(val data: T) : ViewState<T>()
	data class Success<T>(val data: T) : ViewState<T>()
	data class Error(val message: String) : ViewState<Nothing>()
	data class SilentError<T>(val message: String, val data: T) : ViewState<T>()

	fun dataOrNull(): T? {
		return when (this) {
			is Loading -> null
			is SilentLoading -> data
			is Success -> data
			is Error -> null
			is SilentError -> data
		}
	}

	fun toError(message: String): ViewState<T> {
		return when (this) {
			is Loading -> Error(message)
			is SilentLoading -> SilentError(message, data)
			is Success -> SilentError(message, data)
			is Error -> Error(message)
			is SilentError -> SilentError(message, data)
		}
	}
}
