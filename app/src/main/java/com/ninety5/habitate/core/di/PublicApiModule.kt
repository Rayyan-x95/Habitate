package com.ninety5.habitate.core.di

import com.ninety5.habitate.data.remote.publicapis.BooksApi
import com.ninety5.habitate.data.remote.publicapis.FoodApi
import com.ninety5.habitate.data.remote.publicapis.QuotesApi
import com.ninety5.habitate.data.remote.publicapis.WeatherApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PublicApiModule {

    private fun createRetrofit(baseUrl: String, client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("WeatherRetrofit")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return createRetrofit("https://api.open-meteo.com/", okHttpClient, moshi)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(@Named("WeatherRetrofit") retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    @Named("QuotesRetrofit")
    fun provideQuotesRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return createRetrofit("https://zenquotes.io/", okHttpClient, moshi)
    }

    @Provides
    @Singleton
    fun provideQuotesApi(@Named("QuotesRetrofit") retrofit: Retrofit): QuotesApi {
        return retrofit.create(QuotesApi::class.java)
    }

    @Provides
    @Singleton
    @Named("BooksRetrofit")
    fun provideBooksRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return createRetrofit("https://openlibrary.org/", okHttpClient, moshi)
    }

    @Provides
    @Singleton
    fun provideBooksApi(@Named("BooksRetrofit") retrofit: Retrofit): BooksApi {
        return retrofit.create(BooksApi::class.java)
    }

    @Provides
    @Singleton
    @Named("FoodRetrofit")
    fun provideFoodRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return createRetrofit("https://www.themealdb.com/", okHttpClient, moshi)
    }

    @Provides
    @Singleton
    fun provideFoodApi(@Named("FoodRetrofit") retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }
}
