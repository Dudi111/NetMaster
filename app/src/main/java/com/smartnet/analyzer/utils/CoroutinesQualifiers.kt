package com.smartnet.analyzer.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoScope

object CoroutineHelper {

    /**
     * getSupervisorScope: This method will create coroutine scope with supervisor job
     * @param coroutineDispatcher: This parameter contains dispatcher object which can be IO, MAIN or DEFAULT
     * @return CoroutineScope: CoroutineScope with supervisor job
     */
    fun getSupervisorScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(coroutineDispatcher + SupervisorJob())
    }

    /**
     * getNormalScope: This method will create coroutine scope with normal job
     * @param coroutineDispatcher: This parameter contains dispatcher which can be IO, MAIN or DEFAULT
     * @return CoroutineScope: CoroutineScope with normal job
     */
    fun getNormalScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope {
        return CoroutineScope(coroutineDispatcher +  Job())
    }
}