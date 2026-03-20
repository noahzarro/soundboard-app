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
	 * Extract file extension from a URL or file path
	 * @param path The URL or file path
	 * @return The file extension including the dot (e.g., ".mp3", ".jpg"), or empty string if no extension
	 */
	private fun getFileExtension(path: String): String {
		val lastDot = path.lastIndexOf('.')
		val lastSlash = path.lastIndexOf('/')
		return if (lastDot > lastSlash && lastDot != -1) {
			path.substring(lastDot)
		} else {
			""
		}
	}
	
	/**
	 * Get the sounds directory for a specific soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return The directory for the soundboard's sounds
	 */
	private fun getSoundboardSoundsDir(soundboardId: Long): File {
		return File(soundsDir, soundboardId.toString()).also { it.mkdirs() }
	}
	
	/**
	 * Get the images directory for a specific soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return The directory for the soundboard's images
	 */
	private fun getSoundboardImagesDir(soundboardId: Long): File {
		return File(imagesDir, soundboardId.toString()).also { it.mkdirs() }
	}
	
	/**
	 * Download a file from a URL and save it to the specified directory
	 * @param url The URL to download from
	 * @param fileName The name to save the file as
	 * @param directory The directory to save to
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
	 * @param soundboardId The ID of the soundboard this sound belongs to
	 * @param index The index of the sound in the soundboard
	 * @return The saved file, or null if download failed
	 */
	suspend fun downloadSoundFile(url: String, soundboardId: Long, index: Int): File? {
		val extension = getFileExtension(url)
		val fileName = "$index$extension"
		return downloadFile(url, fileName, getSoundboardSoundsDir(soundboardId))
	}
	
	/**
	 * Download an image file from URL
	 * @param url The URL to download from
	 * @param soundboardId The ID of the soundboard this image belongs to
	 * @param index The index of the image in the soundboard
	 * @return The saved file, or null if download failed
	 */
	suspend fun downloadImageFile(url: String, soundboardId: Long, index: Int): File? {
		val extension = getFileExtension(url)
		val fileName = "$index$extension"
		return downloadFile(url, fileName, getSoundboardImagesDir(soundboardId))
	}
	
	/**
	 * Save bytes to a sound file
	 * @param data The byte array to save
	 * @param soundboardId The ID of the soundboard this sound belongs to
	 * @param index The index of the sound in the soundboard
	 * @param extension The file extension (e.g., ".mp3", ".wav")
	 * @return The saved file
	 */
	suspend fun saveSoundFile(data: ByteArray, soundboardId: Long, index: Int, extension: String): File = withContext(Dispatchers.IO) {
		val fileName = "$index$extension"
		val file = File(getSoundboardSoundsDir(soundboardId), fileName)
		file.writeBytes(data)
		file
	}
	
	/**
	 * Save bytes to an image file
	 * @param data The byte array to save
	 * @param soundboardId The ID of the soundboard this image belongs to
	 * @param index The index of the image in the soundboard
	 * @param extension The file extension (e.g., ".jpg", ".png")
	 * @return The saved file
	 */
	suspend fun saveImageFile(data: ByteArray, soundboardId: Long, index: Int, extension: String): File = withContext(Dispatchers.IO) {
		val fileName = "$index$extension"
		val file = File(getSoundboardImagesDir(soundboardId), fileName)
		file.writeBytes(data)
		file
	}
	
	/**
	 * Read a sound file
	 * @param soundboardId The ID of the soundboard this sound belongs to
	 * @param index The index of the sound in the soundboard
	 * @param extension The file extension (e.g., ".mp3", ".wav")
	 * @return The file, or null if it doesn't exist
	 */
	fun getSoundFile(soundboardId: Long, index: Int, extension: String): File? {
		val fileName = "$index$extension"
		val file = File(getSoundboardSoundsDir(soundboardId), fileName)
		return if (file.exists()) file else null
	}
	
	/**
	 * Read an image file
	 * @param soundboardId The ID of the soundboard this image belongs to
	 * @param index The index of the image in the soundboard
	 * @param extension The file extension (e.g., ".jpg", ".png")
	 * @return The file, or null if it doesn't exist
	 */
	fun getImageFile(soundboardId: Long, index: Int, extension: String): File? {
		val fileName = "$index$extension"
		val file = File(getSoundboardImagesDir(soundboardId), fileName)
		return if (file.exists()) file else null
	}
	
	/**
	 * Check if a sound file exists
	 * @param soundboardId The ID of the soundboard this sound belongs to
	 * @param index The index of the sound in the soundboard
	 * @param extension The file extension (e.g., ".mp3", ".wav")
	 * @return true if the file exists, false otherwise
	 */
	fun soundFileExists(soundboardId: Long, index: Int, extension: String): Boolean {
		val fileName = "$index$extension"
		return File(getSoundboardSoundsDir(soundboardId), fileName).exists()
	}
	
	/**
	 * Check if an image file exists
	 * @param soundboardId The ID of the soundboard this image belongs to
	 * @param index The index of the image in the soundboard
	 * @param extension The file extension (e.g., ".jpg", ".png")
	 * @return true if the file exists, false otherwise
	 */
	fun imageFileExists(soundboardId: Long, index: Int, extension: String): Boolean {
		val fileName = "$index$extension"
		return File(getSoundboardImagesDir(soundboardId), fileName).exists()
	}
	
	/**
	 * Delete a sound file
	 * @param soundboardId The ID of the soundboard this sound belongs to
	 * @param index The index of the sound in the soundboard
	 * @param extension The file extension (e.g., ".mp3", ".wav")
	 * @return true if the file was deleted, false otherwise
	 */
	suspend fun deleteSoundFile(soundboardId: Long, index: Int, extension: String): Boolean = withContext(Dispatchers.IO) {
		val fileName = "$index$extension"
		File(getSoundboardSoundsDir(soundboardId), fileName).delete()
	}
	
	/**
	 * Delete an image file
	 * @param soundboardId The ID of the soundboard this image belongs to
	 * @param index The index of the image in the soundboard
	 * @param extension The file extension (e.g., ".jpg", ".png")
	 * @return true if the file was deleted, false otherwise
	 */
	suspend fun deleteImageFile(soundboardId: Long, index: Int, extension: String): Boolean = withContext(Dispatchers.IO) {
		val fileName = "$index$extension"
		File(getSoundboardImagesDir(soundboardId), fileName).delete()
	}
	
	/**
	 * List all sound files for a soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return List of sound file names
	 */
	fun listSoundFiles(soundboardId: Long): List<String> {
		return getSoundboardSoundsDir(soundboardId).listFiles()?.map { it.name } ?: emptyList()
	}
	
	/**
	 * List all image files for a soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return List of image file names
	 */
	fun listImageFiles(soundboardId: Long): List<String> {
		return getSoundboardImagesDir(soundboardId).listFiles()?.map { it.name } ?: emptyList()
	}
	
	/**
	 * List all sound files across all soundboards
	 * @return List of sound file names
	 */
	fun listAllSoundFiles(): List<String> {
		return soundsDir.listFiles()?.flatMap { soundboardDir ->
			soundboardDir.listFiles()?.map { it.name } ?: emptyList()
		} ?: emptyList()
	}
	
	/**
	 * List all image files across all soundboards
	 * @return List of image file names
	 */
	fun listAllImageFiles(): List<String> {
		return imagesDir.listFiles()?.flatMap { soundboardDir ->
			soundboardDir.listFiles()?.map { it.name } ?: emptyList()
		} ?: emptyList()
	}
	
	/**
	 * Delete all sound files for a soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return Number of files deleted
	 */
	suspend fun deleteAllSoundFiles(soundboardId: Long): Int = withContext(Dispatchers.IO) {
		getSoundboardSoundsDir(soundboardId).listFiles()?.count { it.delete() } ?: 0
	}
	
	/**
	 * Delete all image files for a soundboard
	 * @param soundboardId The ID of the soundboard
	 * @return Number of files deleted
	 */
	suspend fun deleteAllImageFiles(soundboardId: Long): Int = withContext(Dispatchers.IO) {
		getSoundboardImagesDir(soundboardId).listFiles()?.count { it.delete() } ?: 0
	}
	
	/**
	 * Delete all sound files across all soundboards
	 * @return Number of files deleted
	 */
	suspend fun deleteAllSoundFiles(): Int = withContext(Dispatchers.IO) {
		soundsDir.listFiles()?.sumOf { soundboardDir ->
			soundboardDir.listFiles()?.count { it.delete() } ?: 0
		} ?: 0
	}
	
	/**
	 * Delete all image files across all soundboards
	 * @return Number of files deleted
	 */
	suspend fun deleteAllImageFiles(): Int = withContext(Dispatchers.IO) {
		imagesDir.listFiles()?.sumOf { soundboardDir ->
			soundboardDir.listFiles()?.count { it.delete() } ?: 0
		} ?: 0
	}
	
	/**
	 * Get the total size of all sound files for a soundboard in bytes
	 * @param soundboardId The ID of the soundboard
	 * @return Total size in bytes
	 */
	fun getSoundFilesSize(soundboardId: Long): Long {
		return getSoundboardSoundsDir(soundboardId).listFiles()?.sumOf { it.length() } ?: 0L
	}
	
	/**
	 * Get the total size of all image files for a soundboard in bytes
	 * @param soundboardId The ID of the soundboard
	 * @return Total size in bytes
	 */
	fun getImageFilesSize(soundboardId: Long): Long {
		return getSoundboardImagesDir(soundboardId).listFiles()?.sumOf { it.length() } ?: 0L
	}
	
	/**
	 * Get the total size of all sound files across all soundboards in bytes
	 * @return Total size in bytes
	 */
	fun getAllSoundFilesSize(): Long {
		return soundsDir.listFiles()?.sumOf { soundboardDir ->
			soundboardDir.listFiles()?.sumOf { it.length() } ?: 0L
		} ?: 0L
	}
	
	/**
	 * Get the total size of all image files across all soundboards in bytes
	 * @return Total size in bytes
	 */
	fun getAllImageFilesSize(): Long {
		return imagesDir.listFiles()?.sumOf { soundboardDir ->
			soundboardDir.listFiles()?.sumOf { it.length() } ?: 0L
		} ?: 0L
	}
}
