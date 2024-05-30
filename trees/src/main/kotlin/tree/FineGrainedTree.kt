package tree

import kotlinx.coroutines.sync.Mutex
import node.TreeNode

class FineGrainedTree<T : Comparable<T>> : AbstractTree<T>() {
    private val mutex = Mutex()

    private suspend fun searchNode(key: T): T? {
        var cur = root
        while (true) {
            // return null if root doesn't exist
            if (cur == null) {
                cur?.unlock()
                return null
            } else if (key == cur.key) {
                cur.unlock()
                return cur.key
            } else if (key < cur.key) {
                val left = cur.left
                left?.lock()
                cur.unlock()
                cur = left
            } else {
                val right = cur.right
                right?.lock()
                cur.unlock()
                cur = right
            }
        }
    }

    override suspend fun search(key: T): T? {
        mutex.lock()
        if (root == null) {
            mutex.unlock()
            return null
        } else {
            root!!.lock()
            searchNode(key)
            mutex.unlock()

            return searchNode(key)
        }
    }


    private fun createNewNode(key: T) = TreeNode(key)

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


    override suspend fun delete(key: T): T? {
       TODO()
    }

    private fun deleteNode(node: TreeNode<T>): TreeNode<T>? {
        return when {
            node.left == null && node.right == null ->
                deleteLeafNode(node)

            node.left == null || node.right == null ->
                deleteNodeWithOneChild(node)

            else -> deleteNodeWithTwoChildren(node)
        }
    }

    private fun replaceChild(wasChild: TreeNode<T>, newChild: TreeNode<T>?) {
        val parent = wasChild.parent
        if (parent == null) {
            root = newChild
            parent?.unlock()
        } else if (parent.left == wasChild) {
            parent.left = newChild
            parent.unlock()
        } else {
            parent.right = newChild
            parent.unlock()
        }

        newChild?.parent = wasChild.parent
    }

    private fun deleteLeafNode(node: TreeNode<T>): TreeNode<T> {
        replaceChild(node, null)
        return node
    }

    private fun deleteNodeWithOneChild(node: TreeNode<T>): TreeNode<T> {
        val nodeToReplaceWith = if (node.left == null) node.right else node.left
        replaceChild(node, nodeToReplaceWith)
        return node
    }

    private fun findPredecessor(node: TreeNode<T>): TreeNode<T> {
        var nodeToReplaceWith = node.left
            ?: throw IllegalStateException("node must have two children")
        while (nodeToReplaceWith.right != null) {
            nodeToReplaceWith = nodeToReplaceWith.right
                ?: throw IllegalStateException("nodeToReplaceWith must have right child")
        }
        return nodeToReplaceWith
    }

    private fun deleteNodeWithTwoChildren(node: TreeNode<T>): TreeNode<T>? {
        val nodePredecessor = findPredecessor(node)
        node.key = nodePredecessor.key
        return deleteNode(nodePredecessor)
    }
}