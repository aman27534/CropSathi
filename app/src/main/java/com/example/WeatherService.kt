package com.example

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val list: List<ForecastItem>
)

@JsonClass(generateAdapter = true)
data class ForecastItem(
    val dt: Long,
    val main: MainData,
    val weather: List<WeatherData>
)

@JsonClass(generateAdapter = true)
data class MainData(
    val temp: Float,
    val temp_max: Float
)

@JsonClass(generateAdapter = true)
data class WeatherData(
    val id: Int,
    val main: String,
    val description: String
)

interface WeatherApi {
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

object WeatherNetwork {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: WeatherApi by lazy {
        retrofit.create(WeatherApi::class.java)
    }
}
