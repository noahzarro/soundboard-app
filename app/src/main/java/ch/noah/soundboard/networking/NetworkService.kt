package ch.noah.soundboard.networking

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url


interface SoundBoardService {
	@GET
	@Headers("Cache-Control: no-cache")
	suspend fun getSoundBoard(@Url url: String): SoundboadDto
}
