package ch.noah.soundboard.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.networking.NetworkRepository
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    //private val stateMutable = MutableStateFlow<Todo?>(null)
    //val state: StateFlow<Todo?> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val soundBoard = NetworkRepository.api.getSoundBoard("stronghold")
            Log.d(
                "MainViewModel",
                "Loaded soundboard: ${soundBoard.title} with ${soundBoard.soundboardItems.size} items"
            )
        }
    }

}