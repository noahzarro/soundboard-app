package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.database.SoundItems
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoardViewModel(context: Context, private val soundBoardId: String) : ViewModel() {

	private val fileStorageRepository = FileStorageRepository(context)
	private val databaseRepository = DatabaseRepository(context)

	private val soundBoardItemsMutable = MutableStateFlow<ViewState<List<SoundItems>>>(ViewState.Loading)
	val soundBoardItems = soundBoardItemsMutable.asStateFlow()

	init {
		loadSoundBoardItems()
	}

	private fun loadSoundBoardItems() {
		viewModelScope.launch {

			val items = databaseRepository.getSoundItemsByBoardId(soundBoardId)
			soundBoardItemsMutable.value = ViewState.Success(items)
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