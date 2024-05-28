package tree

import node.TreeNode

abstract class AbstractTree<E : Comparable<E>> {
    var root: TreeNode<E>? = null

    open suspend fun search(data: E): E? = searchNode(data)?.data

    open fun insert(data: E) = insertNode(data)

    private fun searchNode(data: E): TreeNode<E>? {
        var currentNode = root
        while (currentNode != null) {
            val res = data.compareTo(currentNode.data)
            currentNode = when {
                res < 0 -> currentNode.left
                res > 0 -> currentNode.right
                else -> return currentNode
            }
        }
        return null
    }

    protected abstract fun createNewNode(data: E): TreeNode<E>

    private fun insertNode(data: E): TreeNode<E>? {
        if (root == null) {
            val createdNode = createNewNode(data)
            root = createdNode
            return createdNode
        }

        var currentNode = root
            ?: throw IllegalStateException("Case when the root is null is processed above")
        while (true) {
            val res = data.compareTo(currentNode.data)
            if (res < 0) {
                if (currentNode.left == null) {
                    val createdNode = createNewNode(data)
                    currentNode.left = createdNode
                    createdNode.parent = currentNode
                    return createdNode
                }
                currentNode = currentNode.left
                    ?: throw IllegalStateException("Case when the left child of the currentNode is null is processed above")
            } else if (res > 0) {
                if (currentNode.right == null) {
                    val createdNode = createNewNode(data)
                    currentNode.right = createdNode
                    createdNode.parent = currentNode
                    return createdNode
                }
                currentNode = currentNode.right
                    ?: throw IllegalStateException("Case when the right child of the currentNode is null is processed above")
            } else {
                currentNode.data = data
                return null
            }
        }
    }

    open fun delete(data: E): E? {
        val node = searchNode(data) ?: return null
        val dataToDelete = node.data

        deleteNode(node)
        return dataToDelete
    }

    private fun deleteNode(node: TreeNode<E>): TreeNode<E> {
        return when {
            node.left == null && node.right == null ->
                deleteLeafNode(node)

            node.left == null || node.right == null ->
                deleteNodeWithOneChild(node)

            else -> deleteNodeWithTwoChildren(node)
        }
    }

    private fun replaceChild(wasChild: TreeNode<E>, newChild: TreeNode<E>?) {
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

    private fun findPredecessor(node: TreeNode<E>): TreeNode<E> {
        var nodeToReplaceWith = node.left
            ?: throw IllegalStateException("node must have two children")
        while (nodeToReplaceWith.right != null) {
            nodeToReplaceWith = nodeToReplaceWith.right
                ?: throw IllegalStateException("nodeToReplaceWith must have right child")
        }
        return nodeToReplaceWith
    }

    private fun deleteLeafNode(node: TreeNode<E>): TreeNode<E> {
        replaceChild(node, null)
        return node
    }

    private fun deleteNodeWithOneChild(node: TreeNode<E>): TreeNode<E> {
        val nodeToReplaceWith = if (node.left == null) node.right else node.left
        replaceChild(node, nodeToReplaceWith)
        return node
    }


    private fun deleteNodeWithTwoChildren(node: TreeNode<E>): TreeNode<E> {
        val nodePredecessor = findPredecessor(node)
        // replace data and delete predecessor
        node.data = nodePredecessor.data
        return deleteNode(nodePredecessor)
    }
}