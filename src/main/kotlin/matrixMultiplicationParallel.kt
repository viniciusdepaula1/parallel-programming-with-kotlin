import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

suspend fun matrixMultiplicationParallel() {
    val lineA = 3
    val columnA = 3
    val lineB = 3
    val columnB = 3

    val matrixA = Array(lineA) {Array(columnA) { Random.nextInt(0, 100)} }
    val matrixB = Array(lineB) {Array(columnB) { Random.nextInt(0, 100)} }
    val matrixC = Array(lineA) {Array(columnB) {0} }

    for (i in 0 until lineA) {
        for (j in 0 until columnB) {
            runBlocking {
                var results = matrixA[i].zip(getColumn(j, matrixB)) {itemA, itemB -> Pair (itemA, itemB)}

                val chunksCount = Runtime.getRuntime().availableProcessors()
                var chunkSize = 2

                if(results.size >= chunksCount*2)
                    chunkSize = ceil(results.size / (chunksCount.toDouble())).toInt()

                matrixC[i][j] = results.chunked(chunkSize).map { sublist ->
                    CoroutineScope(dispatcher).async {
                        sublist.map { (a, b) ->
                            a * b
                        }.reduceParallel { acc: Int, item: Int -> acc + item }
                    }
                }.map { it.await() }.reduceParallel { acc: Int, item: Int -> acc + item }

            }
        }
    }

    printMatrix(matrixC)

}

fun printMatrix(matrix: Array<Array<Int>>) {
    for (element in matrix) {
        for (j in 0 until matrix[0].size) {
            print(element[j].toString() + " ")
        }
        println()
    }
    println()
}

fun getColumn(index: Int, matrix: Array<Array<Int>>) : MutableList<Int>{
    var list = mutableListOf<Int>()
    for (i in matrix.indices)
        list.add(matrix[i][index])
    return list
}
