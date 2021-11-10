package com.techriz.andronix.donation.di

import com.techriz.andronix.donation.api.CommerceStatusAPI
import com.techriz.andronix.donation.api.PurchaseAPI
import com.techriz.andronix.donation.utils.Constants.COMMERCE_API_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@ExperimentalCoroutinesApi
class NetworkModule {
    @Provides
    @Singleton
    fun providesLoggingInterceptor(): OkHttpClient {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder().apply {
            addInterceptor(interceptor)
        }.build()
    }

    @CommerceRetrofitInstance
    @Provides
    @Singleton
    fun providesCommerceRetrofit(
        loggingClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(COMMERCE_API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(loggingClient)
        .build()


    @Provides
    @Singleton
    fun providesCommerceStatusVerificationAPI(
        @CommerceRetrofitInstance retrofit: Retrofit
    ): CommerceStatusAPI {
        return retrofit.create(CommerceStatusAPI::class.java)
    }


    @Provides
    @Singleton
    fun providesPurchaseVerificationAPI(
        @CommerceRetrofitInstance retrofit: Retrofit
    ): PurchaseAPI {
        return retrofit.create(PurchaseAPI::class.java)
    }


}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommerceRetrofitInstance
