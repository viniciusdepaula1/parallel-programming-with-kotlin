/**
 * All credits to https://github.com/cvb941/kotlin-parallel-operations
 * for the code base
 */

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlin.random.Random
import kotlin.collections.*
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val cpuCores: Int = Runtime.getRuntime().availableProcessors();
    val randomValues = List(100) { Random.nextInt(0, 9999) }

    val totalTimeParallel = measureTimeMillis {
        runBlocking {
            randomValues.reduceParallel { acc: Int, item: Int -> acc + item }
        }
    }

    println("Number of cores used: $cpuCores")
    println("Total time parallel: $totalTimeParallel")

    val totalTimeSingle = measureTimeMillis {
        randomValues.reduce { acc: Int, item: Int -> acc + item }
    }

    println("Total time single: $totalTimeSingle")
    println("result without thread: ${randomValues.reduce { acc: Int, item: Int -> acc + item }}")
}

suspend fun <T> Iterable<T>.reduceParallel(
    chunkSize: Int,
    operation: (T, T) -> T
): T = chunked(chunkSize).map { subChunk ->
    CoroutineScope(Default).async {
        println("I am running on ${Thread.currentThread().name}")
        subChunk.reduce { acc, t -> operation(acc, t) }
    }
}.map { it.await() }.reduce{ acc, t -> operation(acc, t) }

suspend inline fun <T> Collection<T>.reduceParallel(
    chunksCount: Int = Runtime.getRuntime().availableProcessors(),
    noinline operation: (T, T) -> T
): T {
    if (chunksCount <= 0)
        throw IllegalArgumentException("chunksCount must be a positive integer")
    val chunkSize = ceil(size / chunksCount.toDouble()).toInt()
    return asIterable().reduceParallel(chunkSize, operation)
}