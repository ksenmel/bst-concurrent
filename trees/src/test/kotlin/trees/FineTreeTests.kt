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

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun deletingValuesTwoThreadsTest() {
        val valuesToRemove1 = List(10) { Random.nextInt(100) }
        val valuesToRemove2 = List(10) { Random.nextInt(100) }
        runBlocking {
            val j1 = launch(newSingleThreadContext("thread1")) {
                valuesToRemove1.forEach { tree.add(it) }
            }
            val j2 = launch(newSingleThreadContext("thread2")) {
                valuesToRemove2.forEach { tree.add(it) }
            }
            j1.join()
            j2.join()

            launch(newSingleThreadContext("Thread1")) {
                valuesToRemove1.forEach { tree.delete(it) }
            }
            launch(newSingleThreadContext("Thread2")) {
                valuesToRemove2.forEach { tree.delete(it) }
            }
        }
        Assertions.assertEquals(tree.getValues(), emptyList<Int>())
    }

}