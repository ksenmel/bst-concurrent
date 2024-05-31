package tree

import kotlinx.coroutines.sync.Mutex
import node.TreeNode

class OptimisticTree<T : Comparable<T>> : AbstractTree<T>() {
    private val mutex = Mutex()

    override suspend fun add(key: T) {

        mutex.lock()

        if (root == null) {
            root = createNewNode(key)
            mutex.unlock()
        } else {
            root!!.lock()
            insertNode(key)
            mutex.unlock()
        }
    }
    private fun createNewNode(key: T) = TreeNode(key)

    private suspend fun insertNode(key: T): TreeNode<T>? {
        var cur = root ?: throw IllegalStateException("case when the root is null is processed")

        while (true) {
            if (key < cur.key) {
                if (cur.left == null) {
                    val createdNode = createNewNode(key)
                    cur.left = createdNode
                    createdNode.parent = cur
                    cur.unlock()

                    return createdNode
                } else {
                    val next = cur.left
                        ?: throw IllegalStateException("case when the left child of the current node is null is processed")
                    next.lock()
                    cur = next
                    val parent = cur.parent
                    parent!!.unlock()
                }

            } else if (key > cur.key) {
                if (cur.right == null) {
                    val createdNode = createNewNode(key)
                    cur.right = createdNode
                    createdNode.parent = cur
                    cur.unlock()

                    return createdNode
                } else {
                    val next = cur.right
                        ?: throw IllegalStateException("case when the right child of the current node is null is processed ")
                    next.lock()
                    cur = next
                    val parent = cur.parent
                    parent!!.unlock()
                }
            } else {
                return cur
            }
        }
    }

    override suspend fun search(key: T): T? {
        return searchNode(key)?.key
    }

    private fun validate(node: TreeNode<T>?): Boolean {
        if (root == null) return false
        var cur = root
        while (true) {
            if (cur == node) {
                return true
            } else if (cur?.key!! < node!!.key) {
                cur = cur.left
            } else {
                cur = cur.right
            }
        }
    }

    private suspend fun searchNode(key: T): TreeNode<T>? {
        var cur = root
        while (true) {
            if (cur == null) {
                return null
            }
            if (key == cur.key) {
                cur.lock()
                //check whether the node value has changed
                if (validate(cur) && cur.key == key) {
                    cur.unlock()
                    return cur
                } else {
                    cur.unlock()
                    return null
                }
            } else if (key < cur.key) {
                cur = cur.left
            } else {
                cur = cur.right
            }
        }
    }

    override suspend fun delete(key: T): T? {
        TODO()
    }

}