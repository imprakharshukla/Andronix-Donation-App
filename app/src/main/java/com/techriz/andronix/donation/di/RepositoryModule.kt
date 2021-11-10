package com.techriz.andronix.donation.di

import android.content.Context
import com.techriz.andronix.donation.api.CommerceStatusAPI
import com.techriz.andronix.donation.api.PurchaseAPI
import com.techriz.andronix.donation.repository.BillingRepository
import com.techriz.andronix.donation.repository.PurchaseRepository
import com.techriz.andronix.donation.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@ExperimentalCoroutinesApi
object RepositoryModule {

    @Provides
    @Singleton
    fun providesPurchaseRepository(
    ): PurchaseRepository {
        return PurchaseRepository()
    }


    @Provides
    @Singleton
    fun providesBillingRepository(
        purchaseRepository: PurchaseRepository,
        @ApplicationContext context: Context,
        purchaseAPI: PurchaseAPI,
        commerceStatusAPI: CommerceStatusAPI
    ): BillingRepository {
        return BillingRepository(context, purchaseAPI, purchaseRepository, commerceStatusAPI)
    }

    @Provides
    @Singleton
    fun providesSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }
}