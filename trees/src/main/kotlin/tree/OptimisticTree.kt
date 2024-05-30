package tree

import kotlinx.coroutines.sync.Mutex

abstract class OptimisticTree<T : Comparable<T>> : AbstractTree<T>() {
    private val rootMutex = Mutex()
    override suspend fun search(key: T): T? {
        TODO("Not yet implemented")
    }

    override suspend fun add(key: T) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(key: T): T? {
        TODO("Not yet implemented")
    }

}