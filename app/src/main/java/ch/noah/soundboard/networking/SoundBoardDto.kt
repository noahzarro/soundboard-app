package ch.noah.soundboard.networking

import ch.noah.soundboard.database.SoundBoards
import kotlinx.serialization.Serializable

@Serializable
data class SoundboadDto(
	val title: String,
	val version: String,
	val configUrl: String,
	val items: List<SoundboadItemDto>,
) {

	companion object {
		fun fromDatabaseEntity(entity: SoundBoards): SoundboadDto {
			return SoundboadDto(
				title = entity.title,
				version = entity.version,
				configUrl = entity.configUrl,
				items = emptyList() // Items are not stored in the database, so we return an empty list here
			)
		}
	}

	fun isNewerThan(other: SoundboadDto): Boolean {
		val major = version.substringBefore(".").toIntOrNull() ?: 0
		val minor = version.substringAfter(".").toIntOrNull() ?: 0
		val otherMajor = other.version.substringBefore(".").toIntOrNull() ?: 0
		val otherMinor = other.version.substringAfter(".").toIntOrNull() ?: 0
		return major > otherMajor || (major == otherMajor && minor > otherMinor)
	}

	fun getRootUrl(): String {
		return configUrl.substringBeforeLast("/")
	}
}