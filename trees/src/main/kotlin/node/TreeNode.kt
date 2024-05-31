package node
import kotlinx.coroutines.sync.Mutex
class TreeNode<T: Comparable<T>>(var key: T) {

    var left: TreeNode<T>? = null
    var right: TreeNode<T>? = null
    var parent: TreeNode<T>? = null

    private val mutex = Mutex()
    suspend fun lock() = mutex.lock()
    fun unlock() = mutex.unlock()
}