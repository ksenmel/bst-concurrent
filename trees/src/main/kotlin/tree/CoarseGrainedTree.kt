package tree

import tree.AbstractTree
import node.TreeNode
import kotlinx.coroutines.sync.Mutex

// Грубая

class CoarseGrainedTree<T : Comparable<T>> : AbstractTree<T>() {

    private val rootMutex = Mutex()
    override suspend fun search(key: T) {
        TODO("Not yet implemented")
    }

    override suspend fun add(key: T) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(key: T) {
        TODO("Not yet implemented")
    }

}