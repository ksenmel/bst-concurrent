package node

abstract class TreeNode<T: Comparable<T>>(var key: T) {

    var left: TreeNode<T>? = null
    var right: TreeNode<T>? = null
}