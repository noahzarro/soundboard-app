package ch.noah.soundboard.networking

import kotlinx.serialization.Serializable

@Serializable
data class SoundboadItemDto(
	val name: String,
	val soundPath: String,
	val imagePath: String,
) {
	fun getSoundFileName(): String {
		return soundPath.substringAfterLast("/")
	}

	fun getImageFileName(): String {
		return imagePath.substringAfterLast("/")
	}
}