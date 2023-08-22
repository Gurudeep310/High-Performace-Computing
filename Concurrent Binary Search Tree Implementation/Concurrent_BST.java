import java.util.concurrent.atomic.AtomicReference;

public class Concurrent_BST<T extends Comparable<T>> {
    private final AtomicReference<Node<T>> root = new AtomicReference<>();

    private static class Node<T> {
        T value;
        Node<T> left;
        Node<T> right;

        public Node(T value) {
            this.value = value;
        }
    }

    public boolean contains(T value) {
        Node<T> node = root.get();
        while (node != null) {
            int cmp = value.compareTo(node.value);
            if (cmp == 0) {
                return true;
            } else if (cmp < 0) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return false;
    }

    public boolean insert(T value) {
        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> node = root.get();
            if (node == null) {
                if (root.compareAndSet(null, newNode)) {
                    return true;
                }
            } else {
                int cmp = value.compareTo(node.value);
                if (cmp == 0) {
                    return false;
                } else if (cmp < 0) {
                    if (node.left == null) {
                        if (root.compareAndSet(node, newNode)) {
                            newNode.left = null;
                            newNode.right = null;
                            return true;
                        }
                    } else {
                        node = node.left;
                    }
                } else {
                    if (node.right == null) {
                        if (root.compareAndSet(node, newNode)) {
                            newNode.left = null;
                            newNode.right = null;
                            return true;
                        }
                    } else {
                        node = node.right;
                    }
                }
            }
        }
    }

    public boolean remove(T value) {
        while (true) {
            Node<T> node = root.get();
            if (node == null) {
                return false;
            }
            int cmp = value.compareTo(node.value);
            if (cmp == 0) {
                Node<T> left = node.left;
                Node<T> right = node.right;

                if (left == null) {
                    if (right == null) {
                        /*
                        If the current node has no left or right child,it means the current node is a leaf node. 
                        If no other thread has modified the tree, it tries to set the root to null using root.compareAndSet(node, null). 
                        If successful, it returns true to indicate the successful removal of the value.
                        */
                        if (root.compareAndSet(node, null)) {
                            return true;
                        }
                    } 
                    /*
                      If the current node has a right child but no left child, 
                      It replaces the current node with its right child using root.compareAndSet(node, right), where right is the right child. If successful, it returns true.
                     */
                    
                    else if (root.compareAndSet(node, right)) {
                        return true;
                    }
                } 
                /*
                  If the current node has a left child but no right child, 
                  It replaces the current node with its left child using root.compareAndSet(node, left), where left is the left child. If successful, it returns true.
                 */
                else if (right == null) {
                    if (root.compareAndSet(node, left)) {
                        return true;
                    }
                } 
                /*
                  If the current node has both a left and right child, it finds the minimum node in the right subtree. 
                  It iteratively traverses the left child of the right subtree until it reaches the minimum node, 
                  keeping track of the parent node (minParent) during traversal. 
                  Once found, it rearranges the links to remove the minimum node from its original position. 
                  Then it replaces the current node with the minimum node using root.compareAndSet(node, min), where min is the minimum node. 
                  If successful, it returns true.
                */
                else {
                    Node<T> min = right;
                    Node<T> minParent = node;
                    while (min.left != null) {
                        minParent = min;
                        min = min.left;
                    }
                    if (minParent != node) {
                        minParent.left = min.right;
                        min.right = right;
                    }
                    min.left = left;
                    if (root.compareAndSet(node, min)) {
                        return true;
                    }
                }
            }
            /*
              If cmp is less than 0, it means the value is smaller than the current node's value. 
              In this case, it checks if the current node has a left child. 
              If it doesn't, it means the value doesn't exist in the tree, so it returns false. 
              Otherwise, it updates the node to the left child and continues the loop to search in the left subtree.
             */
            else if (cmp < 0) {
                if (node.left == null) {
                    return false;
                } else {
                    node = node.left;
                }
            } 
            /*
              If cmp is greater than 0, it means the value is larger than the current node's value. 
              In this case, it checks if the current node has a right child. 
              If it doesn't, it means the value doesn't exist in the tree, so it returns false. 
              Otherwise, it updates the node to the right child and continues the loop to search in the right subtree.
             */
            else {
                if (node.right == null) {
                    return false;
                } else {
                    node = node.right;
                }
            }
        }
    }

    public static void main(String[] args) {
        Concurrent_BST<Integer> tree = new Concurrent_BST<>();
        int numNodes = 1000000;
        for (int i = 0; i < numNodes; i++) {
            tree.insert(i);
        }

        int[] numThreads = {1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
        for (int num : numThreads) {
            Thread[] threads = new Thread[num];
            long start = System.currentTimeMillis();
            for (int j = 0; j < num; j++) {
                final int startIdx = (j * numNodes) / num;
                final int endIdx = ((j + 1) * numNodes) / num;
                threads[j] = new Thread(() -> {
                    for (int k = startIdx; k < endIdx; k++) {
                        tree.contains(k);
                    }
                });
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Number of threads: " + num + ", Time: " + (end - start) + "ms");
        }
    }
}   
