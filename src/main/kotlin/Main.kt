package org.example

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.example.deferred.asyncGrouped
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

fun main() {
    runBlocking {
        val firstDigit = async {
            delay(1.seconds)
            1
        }

        val secondDigit = async {
            delay(1.seconds)
            2
        }

        val thirdDigit = async(start = CoroutineStart.LAZY) {
            delay(1.seconds)
            3
        }

        measureTime {
            val printer = asyncGrouped(
                dependencies = listOf(firstDigit, secondDigit)
            ) {
                println(firstDigit.await() + secondDigit.await() + thirdDigit.await())
            }

            printer.await()
        }.also { duration -> println("На выполнение ушло ${duration.inWholeSeconds} с.") }
    }
}