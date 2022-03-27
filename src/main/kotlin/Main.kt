import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlin.random.Random
import kotlin.collections.*

fun main(args: Array<String>) {
    val cpuCores: Int = Runtime.getRuntime().availableProcessors();
    val randomValues = List(999999) { Random.nextInt(0, 100) }

    runBlocking {
        println(randomValues.reduceParallel(cpuCores) { acc: Int, item: Int -> acc + item } )
    }
}

suspend fun <T> Iterable<T>.reduceParallel(
    cpuCores: Int,
    operation: (T, T) -> T
): T = chunked(cpuCores).map { subChunk ->
    CoroutineScope(Default).async {
        println("I am running on ${Thread.currentThread().name}")
        subChunk.reduce { acc, t -> operation(acc, t) }
    }
}.map { it.await() }.reduce{ acc, t -> operation(acc, t) }
