package org.example.deferred

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class GroupedDeferred<T>(
    private val baseDeferred: Deferred<T>,
    private val dependencies: List<Deferred<*>>,
    private val originalCoroutineStart: CoroutineStart,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<T> by baseDeferred {

    init {
        if (originalCoroutineStart != CoroutineStart.LAZY) {
            start()
        }
    }

    override fun start(): Boolean {
        CoroutineScope(coroutineContext).runDependencies()
        return baseDeferred.start()
    }

    override suspend fun await(): T {
        if (!isActive)
            CoroutineScope(coroutineContext).runDependencies()

        return baseDeferred.await()
    }

    private fun CoroutineScope.runDependencies() {
        dependencies.forEach { dependency ->
            launch {
                dependency.await()
            }
        }
    }
}

fun <T> CoroutineScope.asyncGrouped(
    context: CoroutineContext = EmptyCoroutineContext,
    coroutineStart: CoroutineStart = CoroutineStart.DEFAULT,
    dependencies: List<Deferred<*>> = emptyList(),
    block: suspend CoroutineScope.() -> T
): Deferred<T> = GroupedDeferred(async(context, CoroutineStart.LAZY, block), dependencies, coroutineStart, context)