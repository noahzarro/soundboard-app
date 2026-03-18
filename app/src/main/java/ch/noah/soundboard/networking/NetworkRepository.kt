package ch.noah.soundboard.networking

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkRepository {
    val api: SoundBoardService = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SoundBoardService::class.java)
}
