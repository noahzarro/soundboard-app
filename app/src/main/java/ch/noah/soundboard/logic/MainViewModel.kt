package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.networking.NetworkRepository
import ch.noah.soundboard.networking.SoundboadDto
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {

	//private val stateMutable = MutableStateFlow<Todo?>(null)
	//val state: StateFlow<Todo?> = _state

	private val databaseRepository = DatabaseRepository(context)
	private val fileStorageRepository = FileStorageRepository(context)

	init {
		load()
	}

	fun update() {
		viewModelScope.launch {
			val soundBoards = databaseRepository.getAllSoundboards()
			soundBoards.forEach { existingSoundBoard ->
				val updatedSoundBoard = try {
					NetworkRepository.api.getSoundBoard(existingSoundBoard.url)
				} catch (e: Exception) {
					Log.e("MainViewModel", "Error updating soundboard with id ${existingSoundBoard.id}", e)
					null
				}

				if (updatedSoundBoard != null) {
					if (updatedSoundBoard.isNewerThan(SoundboadDto.fromDatabaseEntity(existingSoundBoard))) {
						updateSoundBoard(updatedSoundBoard, existingSoundBoard.id)
					}

				}

			}
		}
	}

	private suspend fun updateSoundBoard(soundBoardDto: SoundboadDto, id: Long) {

		databaseRepository.updateSoundboard(
			id = id,
			title = soundBoardDto.title,
			version = soundBoardDto.version,
		)
		soundBoardDto.items.forEachIndexed { index, item ->
			fileStorageRepository.downloadSoundFile(soundBoardDto.rootUrl + "/" + item.soundPath, id, index)
			fileStorageRepository.downloadImageFile(soundBoardDto.rootUrl + "/" + item.imagePath, id, index)
		}
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

				// For this example, we'll use a hardcoded soundboard ID
				// In a real app, you would get this from the database or generate it
				val soundboardId = 1L

				soundBoard.items.forEachIndexed { index, item ->
					fileStorageRepository.downloadSoundFile(soundBoard.rootUrl + "/" + item.soundPath, soundboardId, index)
				}

				soundBoard.items.firstOrNull()?.let { firstItem ->
					val extension = firstItem.soundPath.substringAfterLast(".")
					fileStorageRepository.getSoundFile(soundboardId, 0, ".$extension")?.let { file ->

						val mediaPlayer = MediaPlayer().apply {
							setDataSource(file.path)
							prepare()
							start()

							// Optional: Release resources when playback completes
							setOnCompletionListener {
								release()
							}
						}
					}
				}

			} catch (e: Exception) {
				Log.e("MainViewModel", "Error loading soundboard", e)
			}
		}
	}

}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
			return MainViewModel(context) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}