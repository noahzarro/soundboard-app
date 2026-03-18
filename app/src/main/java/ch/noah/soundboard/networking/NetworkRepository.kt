package ch.noah.soundboard.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object NetworkRepository {
	private val json = Json {
		ignoreUnknownKeys = true
	}

	val api: SoundBoardService = Retrofit.Builder()
		.baseUrl("https://raw.githubusercontent.com/noahzarro/soundboad-data/refs/heads/main/")
		.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
		.build()
		.create(SoundBoardService::class.java)
}

