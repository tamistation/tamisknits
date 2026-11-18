package com.example.tamisknits.features.client.data.di

import com.example.tamisknits.features.client.data.repository.ClientAccountRepository
import com.example.tamisknits.features.client.data.repository.ClientAddressRepository
import com.example.tamisknits.features.client.data.repository.ClientCartRepository
import com.example.tamisknits.features.client.data.repository.ClientCustomizationRepository
import com.example.tamisknits.features.client.data.repository.ClientOrderRepository
import com.example.tamisknits.features.client.data.repository.ClientProductRepository
import com.example.tamisknits.features.client.data.repository.ClientReviewRepository
import com.example.tamisknits.features.client.data.repository.ClientSupportRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    @Singleton
    fun provideClientProductRepository(
        firestore: FirebaseFirestore
    ): ClientProductRepository = ClientProductRepository(firestore)

    @Provides
    @Singleton
    fun provideClientCartRepository(
        firestore: FirebaseFirestore
    ): ClientCartRepository = ClientCartRepository(firestore)

    @Provides
    @Singleton
    fun provideClientOrderRepository(
        firestore: FirebaseFirestore
    ): ClientOrderRepository = ClientOrderRepository(firestore)

    @Provides
    @Singleton
    fun provideClientAddressRepository(
        firestore: FirebaseFirestore
    ): ClientAddressRepository = ClientAddressRepository(firestore)

    @Provides
    @Singleton
    fun provideClientCustomizationRepository(
        firestore: FirebaseFirestore
    ): ClientCustomizationRepository = ClientCustomizationRepository(firestore)

    @Provides
    @Singleton
    fun provideClientReviewRepository(
        firestore: FirebaseFirestore
    ): ClientReviewRepository = ClientReviewRepository(firestore)

    @Provides
    @Singleton
    fun provideClientSupportRepository(
        firestore: FirebaseFirestore
    ): ClientSupportRepository = ClientSupportRepository(firestore)

    @Provides
    @Singleton
    fun provideClientAccountRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ClientAccountRepository = ClientAccountRepository(auth, firestore)
}
