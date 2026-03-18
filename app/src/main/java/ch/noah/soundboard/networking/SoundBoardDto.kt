package ch.noah.soundboard.networking

data class SoundboadDto(
	val title: String,
	val version: String,
	val soundboardItems: List<SoundboadItemDto>,
) {
	fun isNewerThan(other: SoundboadDto): Boolean {
		val major = version.substringBefore(".").toIntOrNull() ?: 0
		val minor = version.substringAfter(".").toIntOrNull() ?: 0
		val otherMajor = other.version.substringBefore(".").toIntOrNull() ?: 0
		val otherMinor = other.version.substringAfter(".").toIntOrNull() ?: 0
		return major > otherMajor || (major == otherMajor && minor > otherMinor)
	}
}