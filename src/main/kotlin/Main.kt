import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.collections.*
import kotlin.system.measureTimeMillis

val NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors()
val dispatcher = Executors
    .newFixedThreadPool(NUMBER_OF_THREADS)
    .asCoroutineDispatcher()

fun main(args: Array<String>) {
    runBlocking {
        parallelMatrixMultiplication()
    }
    dispatcher.close()
}






