package trees

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import tree.FineGrainedTree
import kotlin.random.Random
import kotlin.test.Test

class FineTreeTests<T> {
    private lateinit var tree: FineGrainedTree<Int>

    @BeforeEach
    fun setUp() {
        tree = FineGrainedTree()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun addingValuesTwoThreadsTest() {
        val valuesToAdd1 = List(10) { Random.nextInt(1000) }
        val valuesToAdd2 = List(10) { Random.nextInt(1000) }
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