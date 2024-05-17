package org.example.deferred

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class GroupedDeferred<T>(
    private val parentDeferred: Deferred<T>,
    private val dependencies: List<Deferred<*>>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    actualCoroutineStart: CoroutineStart,
): Deferred<T> by parentDeferred {

    private val dependenciesScope = CoroutineScope(coroutineContext)

    init {
        /* В доке `DeferredCoroutine` сказано, что вызов `start` с `CoroutineStart.LAZY` ничего не делает */
        if (actualCoroutineStart != CoroutineStart.LAZY) {
            start()
        }
    }

    override fun start(): Boolean {
        dependenciesScope.forEachDependency(Deferred<*>::await)
        return parentDeferred.start()
    }

    override suspend fun await(): T {
        if (!isActive)
            dependenciesScope.forEachDependency(Deferred<*>::await)

        return parentDeferred.await()
    }

    override fun cancel(cause: CancellationException?) {
        dependenciesScope.forEachDependency {
            cancel(cause)
        }
        return parentDeferred.cancel(cause)
    }

    private fun CoroutineScope.forEachDependency(
        action: suspend Deferred<*>.() -> Unit
    ) = launch {
        dependencies.forEach { dependency ->
            launch {
                dependency.action()
            }
        }
    }
}

fun <T> CoroutineScope.asyncGrouped(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    dependencies: List<Deferred<*>> = emptyList(),
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val parentDeferred = async(context, CoroutineStart.LAZY, block)
    return GroupedDeferred(parentDeferred, dependencies, context, start)
}