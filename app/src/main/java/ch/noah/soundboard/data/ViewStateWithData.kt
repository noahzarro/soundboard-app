package ch.noah.soundboard.data

sealed class ViewStateWithData<out T> {
	data object Loading : ViewStateWithData<Nothing>()
	data class SilentLoading<T>(val data: T) : ViewStateWithData<T>()
	data class Success<T>(val data: T) : ViewStateWithData<T>()
	data class Error(val message: String) : ViewStateWithData<Nothing>()
	data class SilentError<T>(val message: String, val data: T) : ViewStateWithData<T>()

	fun dataOrNull(): T? {
		return when (this) {
			is Loading -> null
			is SilentLoading -> data
			is Success -> data
			is Error -> null
			is SilentError -> data
		}
	}

	fun toError(message: String): ViewStateWithData<T> {
		return when (this) {
			is Loading -> Error(message)
			is SilentLoading -> SilentError(message, data)
			is Success -> SilentError(message, data)
			is Error -> Error(message)
			is SilentError -> SilentError(message, data)
		}
	}
}
