package com.example.vqcalsnap.di

import com.example.vqcalsnap.data.repository.LocalAuthRepository
import com.example.vqcalsnap.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        localAuthRepository: LocalAuthRepository
    ): AuthRepository
}

