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

    private fun getSuccessor(node: TreeNode<T>?): TreeNode<T>? {
        var successor = node!!.left ?: throw Exception("node is expected to have 2 children")
        while (successor.right != null) {
            successor = successor.right ?: throw Exception("successor node is expected to have the right child")
        }
        return successor
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