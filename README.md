# bst-concurrent

- Hard synchronization: The entire data structure is locked during operations;
- Soft synchronization: Only the current and previous nodes are blocked;
- Optimistic synchronization: a special function validate() is used to minimize the number of locks.
Each implementation is designed to run in a multithreaded environment, allowing for concurrent access to the tree.

## Sources
[A Practical Concurrent Binary Search Tree](https://stanford-ppl.github.io/website/papers/ppopp207-bronson.pdf)
[BST Visualizer](https://github.com/spbu-coding-2022/trees-7)