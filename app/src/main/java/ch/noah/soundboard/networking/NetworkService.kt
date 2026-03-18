package ch.noah.soundboard.networking

import retrofit2.http.GET
import retrofit2.http.Path


interface SoundBoardService {
    @GET("{boardName}.json")
    suspend fun getSoundBoard(@Path("boardName") boardName: String): SoundboadDto
}
