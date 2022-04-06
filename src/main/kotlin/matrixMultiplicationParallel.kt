import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

suspend fun matrixMultiplication() {
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
                val r = multiplyParallel(matrixA[i], getColumn(j, matrixB))
                matrixC[i][j] = r.reduceParallel { acc: Int, item: Int -> acc + item }
            }
        }
    }

    printMatrix(matrixC)

}

suspend fun multiplyParallel(vector1: Array<Int>, vector2: MutableList<Int>) =
    CoroutineScope(dispatcher).async {
        //println("I am running on ${Thread.currentThread().name}")
        return@async vector1.zip(vector2) {itemA, itemB -> itemA * itemB}
    }.await()

fun printMatrix(matrix: Array<Array<Int>>) {
    for (i in 0 until 3) {
        for (j in 0 until 3) {
            print(matrix[i][j].toString() + " ")
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
