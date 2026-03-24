package ch.noah.soundboard.logic

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.data.AddScreenViewState
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.database.SoundBoards
import ch.noah.soundboard.networking.NetworkRepository
import ch.noah.soundboard.networking.SoundboadDto
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(context: Context) : ViewModel() {

	private val databaseRepository = DatabaseRepository(context)
	private val fileStorageRepository = FileStorageRepository(context)

	private val soundBoardsMutable = MutableStateFlow<ViewState<List<SoundBoards>>>(ViewState.Loading)
	val soundBoards = soundBoardsMutable.asStateFlow()

	private val addScreenViewStateMutable = MutableStateFlow<AddScreenViewState>(AddScreenViewState.Success)
	val addScreenViewState = addScreenViewStateMutable.asStateFlow()

	init {
		viewModelScope.launch {
			//load("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/stronghold.json")
			//load("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/attila.json")
			loadFromDisk()
		}
	}

	fun update() {
		viewModelScope.launch {
			val soundBoards = databaseRepository.getAllSoundboards()
			soundBoards.forEach { existingSoundBoard ->
				val updatedSoundBoard = try {
					NetworkRepository.api.getSoundBoard(existingSoundBoard.configUrl)
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

	private suspend fun loadFromDisk() {
		val soundBoards = databaseRepository.getAllSoundboards()
		soundBoardsMutable.value = ViewState.Success(soundBoards)
	}

	private suspend fun updateSoundBoard(soundBoardDto: SoundboadDto, id: String?) {

		id?.let {
			databaseRepository.deleteSoundboardById(id)
			databaseRepository.deleteSoundItemsByBoardId(id)
		}

		val newId = id ?: java.util.UUID.randomUUID().toString()

		databaseRepository.insertSoundboard(
			id = newId,
			title = soundBoardDto.title,
			version = soundBoardDto.version,
			rootUrl = soundBoardDto.configUrl
		)

		soundBoardDto.items.forEach { item ->
			val uuid = java.util.UUID.randomUUID().toString()
			fileStorageRepository.downloadSoundFile(
				uuid,
				item.getSoundFileName(),
				soundBoardDto.getRootUrl() + "/" + item.soundPath
			)
			fileStorageRepository.downloadImageFile(
				uuid,
				item.getImageFileName(),
				soundBoardDto.getRootUrl() + "/" + item.imagePath
			)
			databaseRepository.insertSoundItem(
				id = uuid,
				boardId = newId,
				name = item.name,
				soundFile = item.getSoundFileName(),
				imageFile = item.getImageFileName()
			)
		}
	}

	suspend fun load(configUrl: String) {
		try {
			val soundBoard =
				NetworkRepository.api.getSoundBoard(configUrl)
			Log.d(
				"MainViewModel",
				"Loaded soundboard: ${soundBoard.title} with ${soundBoard.items.size} items"
			)

			val existingSoundBoard = databaseRepository.getSoundboardByConfigUrl(configUrl)

			updateSoundBoard(soundBoard, existingSoundBoard?.id)

		} catch (e: Exception) {
			Log.e("MainViewModel", "Error loading soundboard", e)
			throw e
		}
	}

	fun addSoundboard(url: String) {
		viewModelScope.launch {
			addScreenViewStateMutable.value = AddScreenViewState.Loading
			try {
				load(url)
				loadFromDisk()
				addScreenViewStateMutable.value = AddScreenViewState.Success
			} catch (e: Exception) {
				addScreenViewStateMutable.value = AddScreenViewState.Error(
					e.message ?: "Failed to load soundboard"
				)
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