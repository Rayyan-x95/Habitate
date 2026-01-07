package com.ninety5.habitate.core.di

import com.ninety5.habitate.BuildConfig
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.ninety5.habitate.data.remote.WsMessage
import java.time.Instant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

import com.ninety5.habitate.data.remote.AuthInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(object {
                @ToJson fun toJson(instant: Instant): String = instant.toString()
                @FromJson fun fromJson(string: String): Instant = Instant.parse(string)
            })
            .add(
                PolymorphicJsonAdapterFactory.of(WsMessage::class.java, "type")
                    .withSubtype(WsMessage.NewMessage::class.java, "new_message")
                    .withSubtype(WsMessage.Typing::class.java, "typing")
                    .withSubtype(WsMessage.Presence::class.java, "presence")
                    .withSubtype(WsMessage.Reaction::class.java, "reaction")
            )
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: com.ninety5.habitate.data.remote.TokenAuthenticator
    ): OkHttpClient {
        // Certificate Pinning Configuration
        // Note: Pins are currently placeholders. Update with real SHA-256 hashes for production.
        val certificatePinner = CertificatePinner.Builder()
            .add("api.habitate.app", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("api.habitate.app", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()

        val builder = OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            // .certificatePinner(certificatePinner) // TODO: Uncomment when real pins are available
            .addInterceptor(authInterceptor) // Inject Auth Token
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

                try {
                    val response = chain.proceed(request)

                    // Log HTTP errors to Firebase Crashlytics
                    if (!response.isSuccessful) {
                        Timber.w("HTTP Error: ${response.code} - ${response.message}")
                    }

                    response
                } catch (e: Exception) {
                    Timber.e(e, "Network request failed")
                    throw e
                }
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Certificate Pinning
        // Enabled only for release builds via FeatureFlags
        // SECURITY: Pinning is MANDATORY for Release builds.
        if (com.ninety5.habitate.core.FeatureFlags.CERTIFICATE_PINNING_ENABLED) {
            val certificatePinner = CertificatePinner.Builder()
                .add("api.habitate.app", "sha256/31Xtgr5nwvA7AUsjrvyvDyf+euY7XRdXPRUHgYGmpbY=") // Primary (Leaf)
                .add("api.habitate.app", "sha256/Ipu894p0lradoHNrOb/HVc67Vo3l2RIbVm2j+AfTyKI=") // Backup (Persisted)
                .build()
            builder.certificatePinner(certificatePinner)
        } else {
            // Debug builds: No pinning to allow proxying
            Timber.w("Certificate Pinning DISABLED for Debug build")
        }

        // GUARDRAIL: Fail fast if pinning is disabled in Release
        if (!BuildConfig.DEBUG && !com.ninety5.habitate.core.FeatureFlags.CERTIFICATE_PINNING_ENABLED) {
            throw IllegalStateException("Security Critical: Certificate Pinning MUST be enabled in Release builds!")
        }


        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_API_URL

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): com.ninety5.habitate.data.remote.ApiService {
        return retrofit.create(com.ninety5.habitate.data.remote.ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAiApi(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): com.ninety5.habitate.data.remote.OpenAiApi {
        val openAiRetrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return openAiRetrofit.create(com.ninety5.habitate.data.remote.OpenAiApi::class.java)
    }
}
