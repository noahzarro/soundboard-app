package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.data.ViewStateWithData
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.database.SoundItems
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BoardViewModel(context: Context, private val soundBoardId: String) : ViewModel() {

	private val fileStorageRepository = FileStorageRepository(context)
	private val databaseRepository = DatabaseRepository(context)

	private val soundBoardItemsMutable = MutableStateFlow<ViewStateWithData<List<SoundItems>>>(ViewStateWithData.Loading)
	val soundBoardItems = soundBoardItemsMutable.asStateFlow()
	private val soundBoardNameMutable = MutableStateFlow<String?>(null)
	val soundBoardName = soundBoardNameMutable.asStateFlow()

	init {
		loadSoundBoardItems()
		loadBoardName()
	}

	private fun loadSoundBoardItems() {
		viewModelScope.launch {
			try {
				val items = databaseRepository.getSoundItemsByBoardId(soundBoardId)
				soundBoardItemsMutable.value = ViewStateWithData.Success(items)
			} catch (e: Exception) {
				Log.e("BoardViewModel", "Error loading soundboard items for board id $soundBoardId", e)
				soundBoardItemsMutable.update {
					it.toError("Failed to load soundboard items: ${e.message}")
				}
			}
		}
	}

	private fun loadBoardName() {
		viewModelScope.launch {
			try {
				val board = databaseRepository.getSoundboardById(soundBoardId)
				soundBoardNameMutable.value = board?.title ?: "Unknown Soundboard"
			} catch (e: Exception) {
				Log.e("BoardViewModel", "Error loading soundboard name for id $soundBoardId", e)
				soundBoardNameMutable.value = "Unknown Soundboard"
			}
		}
	}

	fun playSound(item: SoundItems) {
		fileStorageRepository.getSoundPath(item.id, item.soundFile).let { filePath ->
			MediaPlayer().apply {
				setDataSource(filePath)
				prepare()
				start()

				setOnCompletionListener {
					release()
				}
			}
		}
	}
}

class BoardViewModelFactory(private val context: Context, private val soundBoardId: String) :
	ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
			return BoardViewModel(context, soundBoardId) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}