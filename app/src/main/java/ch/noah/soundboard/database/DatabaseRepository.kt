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

	suspend fun getAllSoundboards(): List<Soundboards> = withContext(Dispatchers.IO) {
		queries.selectAll().executeAsList()
	}

	suspend fun getSoundboardById(id: Long): Soundboards? = withContext(Dispatchers.IO) {
		queries.selectById(id).executeAsOneOrNull()
	}

	suspend fun insertSoundboard(id: Long, title: String, version: String, url: String) = withContext(Dispatchers.IO) {
		queries.insert(id, title, version, url)
	}

	suspend fun updateSoundboard(id: Long, title: String, version: String) = withContext(Dispatchers.IO) {
		queries.update(title, version, id)
	}

	suspend fun deleteSoundboardById(id: Long) = withContext(Dispatchers.IO) {
		queries.deleteById(id)
	}

	suspend fun deleteAllSoundboards() = withContext(Dispatchers.IO) {
		queries.deleteAll()
	}
}
