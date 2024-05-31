package trees

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import tree.OptimisticTree
import kotlin.random.Random
import kotlin.test.Test

class OptimisticTreeTests {

    private lateinit var tree: OptimisticTree<Int>

    @BeforeEach
    fun setUp() {
        tree = OptimisticTree()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun addingValuesTwoThreadsTest() {
        val valuesToAdd1 = List(10) { Random.nextInt(100) }
        val valuesToAdd2 = List(10) { Random.nextInt(100) }
        runBlocking {
            launch(newSingleThreadContext("thread1")) {
                valuesToAdd1.forEach { tree.add(it) }
            }
            launch(newSingleThreadContext("thread2")) {
                valuesToAdd2.forEach { tree.add(it) }
            }
        }
        Assertions.assertEquals(tree.getValues(), (valuesToAdd1 + valuesToAdd2).sorted().distinct())
    }
}