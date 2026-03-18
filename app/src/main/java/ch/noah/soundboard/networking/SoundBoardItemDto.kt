package ch.noah.soundboard.networking

import kotlinx.serialization.Serializable

@Serializable
data class SoundboadItemDto(
	val name: String,
	val soundPath: String,
	val imagePath: String,
) {
	fun getFileName(): String {
		return name + soundPath.substringAfterLast(".")
	}
}