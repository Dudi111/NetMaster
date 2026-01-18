package com.smartnet.analyzer.utils

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    /**
     * providesIoDispatcher: Provide instance of Coroutine (Dispatchers.IO)
     */
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * providesMainDispatcher: Provide instance of Coroutine (Dispatchers.MAIN)
     */
    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * provideScope: Provide instance of Coroutine Scope with SupervisorJob.
     */
    @Singleton
    @IoScope
    @Provides
    fun provideScope(): CoroutineScope {
        return CoroutineHelper.getSupervisorScope(Dispatchers.IO)
    }
}