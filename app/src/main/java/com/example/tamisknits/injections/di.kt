package com.example.tamisknits.injections


import com.example.tamisknits.repository.OrderRepository
import com.example.tamisknits.repository.ProductRepository
import com.example.tamisknits.repository.SupportRepository
import com.example.tamisknits.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideProductRepository(
        firestore: FirebaseFirestore
    ): ProductRepository = ProductRepository(firestore)

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore
    ): OrderRepository = OrderRepository(firestore)

    @Provides
    @Singleton
    fun provideSupportRepository(
        firestore: FirebaseFirestore
    ): SupportRepository = SupportRepository(firestore)
}
