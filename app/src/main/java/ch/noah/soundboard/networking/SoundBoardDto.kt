package ch.noah.soundboard.networking

data class SoundboadDto(
    val title: String,
    val soundboardItems: List<SoundboadItemDto>,
)