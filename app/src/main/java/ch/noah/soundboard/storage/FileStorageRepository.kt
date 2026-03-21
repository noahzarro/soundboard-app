package ch.noah.soundboard.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class FileStorageRepository(private val context: Context) {

	private val filesDir: File = context.filesDir

	// Save image file for a specific item
	suspend fun saveImage(itemUUID: String, filename: String, inputStream: InputStream): File = withContext(Dispatchers.IO) {
		val itemDir = File(filesDir, itemUUID)
		if (!itemDir.exists()) {
			itemDir.mkdirs()
		}
		
		val imageFile = File(itemDir, filename)
		imageFile.outputStream().use { output ->
			inputStream.copyTo(output)
		}
		imageFile
	}

	// Save sound file for a specific item
	suspend fun saveSound(itemUUID: String, filename: String, inputStream: InputStream): File = withContext(Dispatchers.IO) {
		val itemDir = File(filesDir, itemUUID)
		if (!itemDir.exists()) {
			itemDir.mkdirs()
		}
		
		val soundFile = File(itemDir, filename)
		soundFile.outputStream().use { output ->
			inputStream.copyTo(output)
		}
		soundFile
	}

	// Load image file for a specific item
	suspend fun loadImage(itemUUID: String, filename: String): File? = withContext(Dispatchers.IO) {
		val imageFile = File(File(filesDir, itemUUID), filename)
		if (imageFile.exists()) imageFile else null
	}

	// Load sound file for a specific item
	suspend fun loadSound(itemUUID: String, filename: String): File? = withContext(Dispatchers.IO) {
		val soundFile = File(File(filesDir, itemUUID), filename)
		if (soundFile.exists()) soundFile else null
	}

	// Delete a specific image file
	suspend fun deleteImage(itemUUID: String, filename: String): Boolean = withContext(Dispatchers.IO) {
		val imageFile = File(File(filesDir, itemUUID), filename)
		imageFile.delete()
	}

	// Delete a specific sound file
	suspend fun deleteSound(itemUUID: String, filename: String): Boolean = withContext(Dispatchers.IO) {
		val soundFile = File(File(filesDir, itemUUID), filename)
		soundFile.delete()
	}

	// Delete all files for a specific item (entire directory)
	suspend fun deleteItemFiles(itemUUID: String): Boolean = withContext(Dispatchers.IO) {
		val itemDir = File(filesDir, itemUUID)
		if (itemDir.exists() && itemDir.isDirectory) {
			itemDir.deleteRecursively()
		} else {
			false
		}
	}

	// Get the file path for an image
	fun getImagePath(itemUUID: String, filename: String): String {
		return File(File(filesDir, itemUUID), filename).absolutePath
	}

	// Get the file path for a sound
	fun getSoundPath(itemUUID: String, filename: String): String {
		return File(File(filesDir, itemUUID), filename).absolutePath
	}

	// Check if image exists
	suspend fun imageExists(itemUUID: String, filename: String): Boolean = withContext(Dispatchers.IO) {
		File(File(filesDir, itemUUID), filename).exists()
	}

	// Check if sound exists
	suspend fun soundExists(itemUUID: String, filename: String): Boolean = withContext(Dispatchers.IO) {
		File(File(filesDir, itemUUID), filename).exists()
	}
}
