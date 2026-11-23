package com.example.tamisknits.features.delivery.data.di

import com.example.tamisknits.features.delivery.data.repository.DeliveryOrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeliveryModule {

    @Provides
    @Singleton
    fun provideDeliveryOrderRepository(
        firestore: FirebaseFirestore
    ): DeliveryOrderRepository = DeliveryOrderRepository(firestore)
}
