package ch.noah.soundboard.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepository(context: Context) {
	private val driver: SqlDriver = AndroidSqliteDriver(
		schema = SoundboardDatabase.Schema,
		context = context,
		name = "soundboard.db"
	)

	private val database = SoundboardDatabase(driver)
	private val queries = database.soundboardsQueries

	// SoundBoards operations
	suspend fun getAllSoundboards(): List<SoundBoards> = withContext(Dispatchers.IO) {
		queries.selectAllBoards().executeAsList()
	}

	suspend fun getSoundboardById(id: String): SoundBoards? = withContext(Dispatchers.IO) {
		queries.selectBoardById(id).executeAsOneOrNull()
	}

	suspend fun insertSoundboard(id: String, title: String, version: String, rootUrl: String) = withContext(Dispatchers.IO) {
		queries.insertBoard(id, title, version, rootUrl)
	}

	suspend fun updateSoundboard(id: String, title: String, version: String, rootUrl: String) = withContext(Dispatchers.IO) {
		queries.updateBoard(title, version, rootUrl, id)
	}

	suspend fun deleteSoundboardById(id: String) = withContext(Dispatchers.IO) {
		queries.deleteBoardById(id)
	}

	suspend fun deleteAllSoundboards() = withContext(Dispatchers.IO) {
		queries.deleteAllBoards()
	}

	// SoundItems operations
	suspend fun getAllSoundItems(): List<SoundItems> = withContext(Dispatchers.IO) {
		queries.selectAllItems().executeAsList()
	}

	suspend fun getSoundItemById(id: String): SoundItems? = withContext(Dispatchers.IO) {
		queries.selectItemById(id).executeAsOneOrNull()
	}

	suspend fun getSoundItemsByBoardId(boardId: String): List<SoundItems> = withContext(Dispatchers.IO) {
		queries.selectItemsByBoardId(boardId).executeAsList()
	}

	suspend fun insertSoundItem(id: String, boardId: String, name: String, soundFile: String, imageFile: String) = withContext(Dispatchers.IO) {
		queries.insertItem(id, boardId, name, soundFile, imageFile)
	}

	suspend fun updateSoundItem(id: String, name: String, soundFile: String, imageFile: String) = withContext(Dispatchers.IO) {
		queries.updateItem(name, soundFile, imageFile, id)
	}

	suspend fun deleteSoundItemById(id: String) = withContext(Dispatchers.IO) {
		queries.deleteItemById(id)
	}

	suspend fun deleteSoundItemsByBoardId(boardId: String) = withContext(Dispatchers.IO) {
		queries.deleteItemsByBoardId(boardId)
	}

	suspend fun deleteAllSoundItems() = withContext(Dispatchers.IO) {
		queries.deleteAllItems()
	}
}
