import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.util.concurrent.Executors

val NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors()
val dispatcher = Executors
    .newFixedThreadPool(NUMBER_OF_THREADS)
    .asCoroutineDispatcher()

var searchTermCount = MutableList(0) {0}
const val bufferSize = 10
val channelLoad = Channel<String>(bufferSize)
val channelDecode = Channel<List<String>>(bufferSize)
val channelTC = Channel<List<String>>(bufferSize)
val channelSC = Channel<Int>(bufferSize)

val fileStream = File("./", "arq1.txt").bufferedReader(bufferSize = 500)

//fun main(args: Array<String>) {
//    runBlocking {
//        matrixMultiplicationParallel()
//    }
//    dispatcher.close()
//}

//LOAD
suspend fun load(cout: Channel<String>) {
    println("LOAD starting")

    repeat(100) {
        File("./", "arq1.txt").readLines().map {
            runBlocking {
                println("LOAD sending $it")
                cout.send(it.toString())
            }
        }
    }

    cout.close()
    println("Source exiting")
}

//DECODE
suspend fun decode(cin: Channel<String>, cout: Channel<List<String>>) {
    println("  DECODE starting")
    for (x in cin) {
        println("  DECODE received $x")

        val y = x.split(" ")
        println("  DECODE sent $y")

        cout.send(y)
    }
    cout.close()
    println("  DECODE exiting")
}

//TERM CLEAN
suspend fun termClean(cin: Channel<List<String>>, cout: Channel<List<String>>) {
    println("    TERMCLEAN starting")
    for (x in cin) {
        println("    TERMCLEAN received $x")

        val y = x.filter { it.length > 3 }
            .map { it.dropLastWhile { !it.isLetter() } }
            .map { it.dropWhile { !it.isLetter() } }

        println("    TERMCLEAN sending $y")
        cout.send(y)

    }
    println("   TERMCLEAN exiting")
    cout.close()
}


//SEARCH AND COUNT
suspend fun searchAndCount(cin: Channel<List<String>>, cout: Channel<Int>, term: String) {
    println("       SAC starting")
    for (x in cin) {
        println("       SAC received $x")
        val y = x.filter { it == term }.size
        println("       SAC sending ${y}")
        cout.send(y)
    }
    println("       SAC exiting")
    cout.close()
}

//REDUCE
suspend fun reducePipe(cin: Channel<Int>) {
    println("           REDUCE starting")
    for (x in cin) {
        println("           REDUCE received $x")
        searchTermCount.add(x)
    }
    println("           REDUCE exiting")
    cin.close()
}

fun runAll() {
    runBlocking {
        withContext(CoroutineScope(dispatcher).coroutineContext) {
            launch { load(channelLoad) }
            launch { decode(channelLoad, channelDecode) }
            launch { termClean(channelDecode, channelTC) }
            launch { searchAndCount(channelTC, channelSC, "idiomas") }
            launch { reducePipe(channelSC) }
        }
    }

    println(runBlocking { searchTermCount.reduceParallel { i, i2 -> i+i2 }})
    println("runAll exiting")
}

fun main() {
    println("main starting")
    runAll()
    println("main exiting")
}




