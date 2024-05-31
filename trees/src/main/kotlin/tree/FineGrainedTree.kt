package tree

import kotlinx.coroutines.sync.Mutex
import node.TreeNode

class FineGrainedTree<T : Comparable<T>> : AbstractTree<T>() {
    private val mutex = Mutex()

    private suspend fun searchNode(key: T): TreeNode<T>? {
        var cur = root
        while (true) {
            // return null if root doesn't exist
            if (cur == null) {
                cur?.unlock()
                return null
            } else if (key == cur.key) {
                cur.unlock()
                return cur
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

            return searchNode(key)?.key
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

    private fun getSuccessor(node: TreeNode<T>?): TreeNode<T>? {
        var successorNode = node!!.left ?: throw Exception("node is expected to have 2 children")
        while (successorNode.right != null) {
            successorNode = successorNode.right ?: throw Exception("successor node is expected to have the right child")
        }
        return successorNode
    }

    private fun deleteNode(node: TreeNode<T>?) {
        if (node!!.left == null && node.right == null) {
            replaceNode(node, null)
        }   else if (node.left == null || node.right == null) {
            replaceNode(node, if (node.left == null) node.right else node.left)
        }    else {
            val successor = getSuccessor(node)
            node.key = successor!!.key
            deleteNode(successor)
        }
    }

    private fun replaceNode(nodeToReplace: TreeNode<T>?, replacementNode: TreeNode<T>?) {
        val parent = nodeToReplace!!.parent
        if (parent == null) {
            root = replacementNode
        } else {
            if (parent.left == nodeToReplace) {
                parent.left = replacementNode
            }
            else {
                parent.right = replacementNode
            }
        }
        replacementNode?.parent = parent
    }


    override suspend fun delete(key: T): T? {
        mutex.lock()
        if (root?.key == key) {
            deleteNode(root!!)
            mutex.unlock()
            return root?.key
        } else if (root != null) {
            root?.lock()
            mutex.unlock()
        } else {
            mutex.unlock()
            return null
        }
        val node = searchNode(key) ?: return null
        if (node == root) {
            deleteNode(node)
            return node.key
        }
        deleteNode(node)
        return node.key
    }
}