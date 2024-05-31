package tree

import node.TreeNode
import kotlinx.coroutines.sync.Mutex

class CoarseGrainedTree<T : Comparable<T>> : AbstractTree<T>() {
    private val mutex = Mutex()

    override suspend fun add(key: T) {
        mutex.lock()
        insertNode(key)
        mutex.unlock()
    }

    private fun createNewNode(key: T) = TreeNode(key)

    private fun insertNode(key: T): TreeNode<T>? {
        if (root == null) {
            val newNode = createNewNode(key)
            root = newNode
            return newNode
        }

        var currentNode = root ?: throw IllegalStateException("case when the root is null is processed above")
        while (true) {
            val res = key.compareTo(currentNode.key)
            if (res < 0) {
                if (currentNode.left == null) {
                    val createdNode = createNewNode(key)
                    currentNode.left = createdNode
                    createdNode.parent = currentNode
                    return createdNode
                }
                currentNode = currentNode.left
                    ?: throw IllegalStateException("case when the left child of the currentNode is null is processed above")
            } else if (res > 0) {
                if (currentNode.right == null) {
                    val createdNode = createNewNode(key)
                    currentNode.right = createdNode
                    createdNode.parent = currentNode
                    return createdNode
                }
                currentNode = currentNode.right
                    ?: throw IllegalStateException("case when the right child of the currentNode is null is processed above")
            } else {
                currentNode.key = key
                return null
            }
        }
    }

    override suspend fun search(key: T): T? {
        mutex.lock()
        searchNode(key)
        mutex.unlock()

        return searchNode(key)?.key
    }

    private fun searchNode(key: T): TreeNode<T>? {
        var currentNode = root
        while (currentNode != null) {
            val res = key.compareTo(currentNode.key)
            currentNode = when {
                res < 0 -> currentNode.left
                res > 0 -> currentNode.right
                else -> return currentNode
            }
        }
        return null
    }

    override suspend fun delete(key: T): T? {
        val node = searchNode(key) ?: return null
        val dataToDelete = node.key
        mutex.lock()
        deleteNode(node)
        mutex.unlock()

        return dataToDelete
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
        } else if (parent.left == wasChild) {
            parent.left = newChild
        } else {
            parent.right = newChild
        }

        newChild?.parent = wasChild.parent
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

    private fun deleteLeafNode(node: TreeNode<T>): TreeNode<T> {
        replaceChild(node, null)
        return node
    }

    private fun deleteNodeWithOneChild(node: TreeNode<T>): TreeNode<T> {
        val nodeToReplaceWith = if (node.left == null) node.right else node.left
        replaceChild(node, nodeToReplaceWith)
        return node
    }

    private fun deleteNodeWithTwoChildren(node: TreeNode<T>): TreeNode<T>? {
        val nodePredecessor = findPredecessor(node)
        node.key = nodePredecessor.key
        return deleteNode(nodePredecessor)
    }
}