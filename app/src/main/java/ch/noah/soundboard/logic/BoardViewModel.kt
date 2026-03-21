package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.networking.SoundboadItemDto
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardViewModel(context: Context, private val soundBoardId: Long) : ViewModel() {

	private val fileStorageRepository = FileStorageRepository(context)

	private val soundBoardItemsMutable = MutableStateFlow<ViewState<List<SoundboadItemDto>>>(ViewState.Loading)
	val soundBoardItems = soundBoardItemsMutable.asStateFlow()

	private fun loadSoundBoardItems() {
		val imageFiles = fileStorageRepository.listImageFiles(soundBoardId)
		val soundFiles = fileStorageRepository.listSoundFiles(soundBoardId)
		soundBoardItemsMutable.value = ViewState.Success(items)
	}

	fun playSound(itemIndex: Int) {
		fileStorageRepository.getSoundFile(soundBoardId, itemIndex)?.let { file ->
			MediaPlayer().apply {
				setDataSource(file.path)
				prepare()
				start()

				setOnCompletionListener {
					release()
				}
			}
		}
	}
}

class BoardViewModelFactory(private val context: Context, private val soundBoardId: Long) :
	ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
			return BoardViewModel(context, soundBoardId) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}