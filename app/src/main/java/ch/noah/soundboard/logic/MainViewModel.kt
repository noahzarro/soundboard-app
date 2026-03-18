package ch.noah.soundboard.logic

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.networking.NetworkRepository
import kotlinx.coroutines.launch


class MainViewModel(context: Context) : ViewModel() {

	//private val stateMutable = MutableStateFlow<Todo?>(null)
	//val state: StateFlow<Todo?> = _state

	private val databaseRepository = DatabaseRepository(context)

	init {
		load()
	}

	fun load() {
		viewModelScope.launch {
			try {
				val soundBoard =
					NetworkRepository.api.getSoundBoard("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/stronghold.json")
				Log.d(
					"MainViewModel",
					"Loaded soundboard: ${soundBoard.title} with ${soundBoard.items.size} items"
				)

			} catch (e: Exception) {
				Log.e("MainViewModel", "Error loading soundboard", e)
			}
		}
	}

}