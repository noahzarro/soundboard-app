package ch.noah.soundboard.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class FileStorageRepository(private val context: Context) {
	
	private val soundsDir: File
		get() = File(context.filesDir, "sounds").also { it.mkdirs() }
	
	private val imagesDir: File
		get() = File(context.filesDir, "images").also { it.mkdirs() }
	
	/**
	 * Download a file from a URL and save it to the specified directory
	 * @param url The URL to download from
	 * @param fileName The name to save the file as
	 * @param directory The directory to save to (soundsDir or imagesDir)
	 * @return The saved file, or null if download failed
	 */
	private suspend fun downloadFile(url: String, fileName: String, directory: File): File? = withContext(Dispatchers.IO) {
		try {
			val connection = URL(url).openConnection()
			connection.connect()
			
			val inputStream: InputStream = connection.getInputStream()
			val file = File(directory, fileName)
			
			FileOutputStream(file).use { output ->
				inputStream.use { input ->
					input.copyTo(output)
				}
			}
			
			file
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}
	
	/**
	 * Download a sound file from URL
	 * @param url The URL to download from
	 * @param fileName The name to save the file as
	 * @return The saved file, or null if download failed
	 */
	suspend fun downloadSoundFile(url: String, fileName: String): File? {
		return downloadFile(url, fileName, soundsDir)
	}
	
	/**
	 * Download an image file from URL
	 * @param url The URL to download from
	 * @param fileName The name to save the file as
	 * @return The saved file, or null if download failed
	 */
	suspend fun downloadImageFile(url: String, fileName: String): File? {
		return downloadFile(url, fileName, imagesDir)
	}
	
	/**
	 * Save bytes to a sound file
	 * @param fileName The name to save the file as
	 * @param data The byte array to save
	 * @return The saved file
	 */
	suspend fun saveSoundFile(fileName: String, data: ByteArray): File = withContext(Dispatchers.IO) {
		val file = File(soundsDir, fileName)
		file.writeBytes(data)
		file
	}
	
	/**
	 * Save bytes to an image file
	 * @param fileName The name to save the file as
	 * @param data The byte array to save
	 * @return The saved file
	 */
	suspend fun saveImageFile(fileName: String, data: ByteArray): File = withContext(Dispatchers.IO) {
		val file = File(imagesDir, fileName)
		file.writeBytes(data)
		file
	}
	
	/**
	 * Read a sound file
	 * @param fileName The name of the file to read
	 * @return The file, or null if it doesn't exist
	 */
	fun getSoundFile(fileName: String): File? {
		val file = File(soundsDir, fileName)
		return if (file.exists()) file else null
	}
	
	/**
	 * Read an image file
	 * @param fileName The name of the file to read
	 * @return The file, or null if it doesn't exist
	 */
	fun getImageFile(fileName: String): File? {
		val file = File(imagesDir, fileName)
		return if (file.exists()) file else null
	}
	
	/**
	 * Check if a sound file exists
	 * @param fileName The name of the file to check
	 * @return true if the file exists, false otherwise
	 */
	fun soundFileExists(fileName: String): Boolean {
		return File(soundsDir, fileName).exists()
	}
	
	/**
	 * Check if an image file exists
	 * @param fileName The name of the file to check
	 * @return true if the file exists, false otherwise
	 */
	fun imageFileExists(fileName: String): Boolean {
		return File(imagesDir, fileName).exists()
	}
	
	/**
	 * Delete a sound file
	 * @param fileName The name of the file to delete
	 * @return true if the file was deleted, false otherwise
	 */
	suspend fun deleteSoundFile(fileName: String): Boolean = withContext(Dispatchers.IO) {
		File(soundsDir, fileName).delete()
	}
	
	/**
	 * Delete an image file
	 * @param fileName The name of the file to delete
	 * @return true if the file was deleted, false otherwise
	 */
	suspend fun deleteImageFile(fileName: String): Boolean = withContext(Dispatchers.IO) {
		File(imagesDir, fileName).delete()
	}
	
	/**
	 * List all sound files
	 * @return List of sound file names
	 */
	fun listSoundFiles(): List<String> {
		return soundsDir.listFiles()?.map { it.name } ?: emptyList()
	}
	
	/**
	 * List all image files
	 * @return List of image file names
	 */
	fun listImageFiles(): List<String> {
		return imagesDir.listFiles()?.map { it.name } ?: emptyList()
	}
	
	/**
	 * Delete all sound files
	 * @return Number of files deleted
	 */
	suspend fun deleteAllSoundFiles(): Int = withContext(Dispatchers.IO) {
		soundsDir.listFiles()?.count { it.delete() } ?: 0
	}
	
	/**
	 * Delete all image files
	 * @return Number of files deleted
	 */
	suspend fun deleteAllImageFiles(): Int = withContext(Dispatchers.IO) {
		imagesDir.listFiles()?.count { it.delete() } ?: 0
	}
	
	/**
	 * Get the total size of all sound files in bytes
	 * @return Total size in bytes
	 */
	fun getSoundFilesSize(): Long {
		return soundsDir.listFiles()?.sumOf { it.length() } ?: 0L
	}
	
	/**
	 * Get the total size of all image files in bytes
	 * @return Total size in bytes
	 */
	fun getImageFilesSize(): Long {
		return imagesDir.listFiles()?.sumOf { it.length() } ?: 0L
	}
}
