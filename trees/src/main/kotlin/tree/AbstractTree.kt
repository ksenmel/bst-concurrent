package tree

import node.TreeNode

abstract class AbstractTree<T : Comparable<T>> {
    var root: TreeNode<T>? = null

    abstract suspend fun search(key: T)
    abstract suspend fun add(key: T)
    abstract suspend fun remove(key: T)
}