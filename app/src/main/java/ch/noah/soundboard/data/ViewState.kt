package ch.noah.soundboard.data

sealed class ViewState {
	data object Success : ViewState()
	data object Loading : ViewState()
	data class Error(val message: String) : ViewState()
}
