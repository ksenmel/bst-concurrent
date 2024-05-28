package node

class TreeNode<E : Comparable<E>>(data: E) {
    var data: E = data

    var parent: TreeNode<E>? = null
    var left: TreeNode<E>? = null
    var right: TreeNode<E>? = null
}