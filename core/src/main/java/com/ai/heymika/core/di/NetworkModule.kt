package com.ai.heymika.core.di

import com.ai.heymika.core.BuildConfig
import com.ai.heymika.core.data.connectivity.NetworkConnectivityObserver
import com.ai.heymika.core.data.source.remote.RemoteDataSource
import com.ai.heymika.core.data.source.remote.network.ApiService
import com.ai.heymika.core.domain.connectivity.ConnectivityObserver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule =
    module {
        single {
            val logging =
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    },
                )
            OkHttpClient
                .Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request =
                        chain
                            .request()
                            .newBuilder()
                            .addHeader("Accept", "application/json")
                            .build()
                    chain.proceed(request)
                }.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                // Generation can take a while on the backend, so reads are patient.
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }
        single {
            Retrofit
                .Builder()
                // Our own backend; it holds the Anthropic key and proxies the model.
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(get())
                .build()
                .create(ApiService::class.java)
        }
        single { RemoteDataSource(get()) }
        single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }
    }

private const val TIMEOUT_SECONDS = 120L
