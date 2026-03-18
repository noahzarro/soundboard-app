package ch.noah.soundboard.networking

import retrofit2.http.GET
import retrofit2.http.Url


interface SoundBoardService {
	@GET
	suspend fun getSoundBoard(@Url url: String): SoundboadDto

	@GET
	suspend fun getSound(@Url url: String): List<SoundboadDto>
}
