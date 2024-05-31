package tree

import node.TreeNode

abstract class AbstractTree<T : Comparable<T>> {
    var root: TreeNode<T>? = null

    abstract suspend fun search(key: T): T?
    abstract suspend fun add(key: T)
    abstract suspend fun delete(key: T): T?

    fun getValues(): List<T> {
        val res = mutableListOf<T>()
        fun getValuesRecursively(currentNode: TreeNode<T>?) {
            currentNode ?: return
            getValuesRecursively(currentNode.left)
            res.add(currentNode.key)
            getValuesRecursively(currentNode.right)
        }
        getValuesRecursively(root)
        return res
    }
}