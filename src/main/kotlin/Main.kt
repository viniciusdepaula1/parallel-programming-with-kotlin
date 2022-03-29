import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlin.random.Random
import kotlin.collections.*
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val cpuCores: Int = Runtime.getRuntime().availableProcessors();
    val randomValues = List(999999) { Random.nextInt(0, 100) }

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
    itr: Int,
    operation: (T, T) -> T
): T = chunked(chunkSize).map { subChunk ->
    CoroutineScope(Default).async {
        //println("I am running on ${Thread.currentThread().name}")
        subChunk.reduce { acc, t -> operation(acc, t) }
    }
}.map { it.await() }.reduceParallel(itr = itr + 1) { acc, t -> operation(acc, t) }

suspend inline fun <T> Collection<T>.reduceParallel(
    chunksCount: Int = Runtime.getRuntime().availableProcessors(),
    itr: Int = 0,
    noinline operation: (T, T) -> T,
): T {
    if(size == 1) {
        println(this.first())
        return this.first()
    }

    if (chunksCount <= 0)
        throw IllegalArgumentException("chunksCount must be a positive integer")

    val chunkSize = ceil(size / (chunksCount.toDouble()/ 2.0.pow(itr))).toInt()

    return asIterable().reduceParallel(chunkSize, itr, operation)
}