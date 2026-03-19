package ch.noah.soundboard.logic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
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
		soundBoardDto.items.forEach {
			fileStorageRepository.downloadSoundFile(soundBoardDto.rootUrl + "/" + it.soundPath, it.getFileName())
			fileStorageRepository.downloadImageFile(soundBoardDto.rootUrl + "/" + it.imagePath, it.getFileName())
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

				soundBoard.items.forEach {
					fileStorageRepository.downloadSoundFile(soundBoard.rootUrl + "/" + it.soundPath, it.getFileName())
				}

				soundBoard.items.first().let {
					fileStorageRepository.getSoundFile(it.getFileName())?.let { file ->

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