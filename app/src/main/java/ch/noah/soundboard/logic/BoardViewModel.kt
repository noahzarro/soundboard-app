package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.noah.soundboard.storage.FileStorageRepository

class BoardViewModel(context: Context, private val soundBoardId: Long) : ViewModel() {

	private val fileStorageRepository = FileStorageRepository(context)

	fun playSound(itemIndex: Int, extension: String) {
		fileStorageRepository.getSoundFile(soundBoardId, itemIndex, extension)?.let { file ->
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