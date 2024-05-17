package org.example

import kotlinx.coroutines.*
import org.example.deferred.asyncGrouped
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

fun main() {
    runBlocking {
        val firstDigit = async(start = CoroutineStart.LAZY) {
            delay(3.seconds)
            println("Выполнился 1")
            1
        }

        val secondDigit = async(start = CoroutineStart.LAZY) {
            delay(4.seconds)
            println("Выполнился 2")
            2
        }

        val thirdDigit = async(start = CoroutineStart.LAZY) {
            delay(5.seconds)
            println("Выполнился 3")
            3
        }

        val dependencies = listOf(firstDigit, secondDigit, thirdDigit)

        measureTime {

            val grouped = asyncGrouped(
                dependencies = dependencies
            ) {

                dependencies.fold(0) { current, next ->
                    current + next.await()
                }.also { println("Результат: $it") }

            }

            delay(2.seconds)

            grouped.cancel()

        }.also { duration -> println("На выполнение ушло ${duration.inWholeSeconds} с.") }
    }
}