package ch.noah.soundboard.logic

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.noah.soundboard.Constants
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.data.ViewStateWithData
import ch.noah.soundboard.database.DatabaseRepository
import ch.noah.soundboard.database.SoundBoards
import ch.noah.soundboard.networking.NetworkRepository
import ch.noah.soundboard.networking.SoundboadDto
import ch.noah.soundboard.storage.FileStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder

class MainViewModel(context: Context) : ViewModel() {

	private val databaseRepository = DatabaseRepository(context)
	private val fileStorageRepository = FileStorageRepository(context)

	private val soundBoardsMutable = MutableStateFlow<ViewStateWithData<List<SoundBoards>>>(ViewStateWithData.Loading)
	val soundBoards = soundBoardsMutable.asStateFlow()

	private val addScreenViewStateMutable = MutableStateFlow<ViewState>(ViewState.Success)
	val addScreenViewState = addScreenViewStateMutable.asStateFlow()

	init {
		viewModelScope.launch {
			//load("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/stronghold.json")
			//load("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/attila.json")
			loadFromDisk()
			update()
		}
	}

	fun deleteSoundboard(id: String) {
		viewModelScope.launch {
			try {
				databaseRepository.deleteSoundboardById(id)
				databaseRepository.deleteSoundItemsByBoardId(id)
				loadFromDisk()
			} catch (e: Exception) {
				Log.e("MainViewModel", "Error deleting soundboard with id $id", e)
				soundBoardsMutable.update {
					it.toError("Failed to delete soundboard: ${e.message}")
				}
			}
		}
	}

	fun update() {
		viewModelScope.launch {
			val soundBoards = databaseRepository.getAllSoundboards()
			var currentSoundBoardTitle = ""
			try {
				soundBoards.forEach { existingSoundBoard ->
					currentSoundBoardTitle = existingSoundBoard.title
					val updatedSoundBoard = try {
						NetworkRepository.api.getSoundBoard(existingSoundBoard.configUrl)
					} catch (e: Exception) {
						Log.e("MainViewModel", "Error updating soundboard with id ${existingSoundBoard.id}", e)
						null
					}

					if (updatedSoundBoard != null) {
						if (updatedSoundBoard.isNewerThan(SoundboadDto.fromDatabaseEntity(existingSoundBoard))) {
							Log.d(
								"MainViewModel",
								"Updating soundboard ${existingSoundBoard.title} to version ${updatedSoundBoard.version}"
							)
							updateSoundBoard(updatedSoundBoard, existingSoundBoard.id)
						}
					}
				}
				loadFromDisk()
			} catch (e: Exception) {
				Log.e("MainViewModel", "Error updating soundboard $currentSoundBoardTitle", e)
				soundBoardsMutable.update {
					it.toError(e.message + ". Failed to update soundboard $currentSoundBoardTitle")
				}
			}
		}
	}

	private suspend fun loadFromDisk() {
		val soundBoards = databaseRepository.getAllSoundboards()
		soundBoardsMutable.value = ViewStateWithData.Success(soundBoards)
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

	fun handleDeepLink(uri: Uri) {
		addSoundboard(uri)
	}

	fun addSoundboard(uri: Uri) {
		val host = uri.host
		val configUrl = if (host == Constants.DEEP_LINK_HOST) {
			val configUrlEncoded = uri.path
			val configUrlDecoded = URLDecoder.decode(configUrlEncoded, "UTF-8")
				?.takeIf { it.isNotBlank() }?.trim('/')
			configUrlDecoded
		} else {
			uri.toString()
		} ?: return

		viewModelScope.launch {
			addScreenViewStateMutable.value = ViewState.Loading
			try {
				load(configUrl.trim())
				loadFromDisk()
				addScreenViewStateMutable.value = ViewState.Success
			} catch (e: Exception) {
				addScreenViewStateMutable.value = ViewState.Error(
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