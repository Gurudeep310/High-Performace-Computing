import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;


public class MySkipListComparison {
    public static void main(String[] args) {
        // Number of threads to test
        int[] numThreads = {1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20};

        for (int numThread : numThreads) {
            testMySkipList(numThread);
        }
    }

    public static void testMySkipList(int numThreads) {
        MySkipList<Integer> mySkipList = new MySkipList<>();
        ConcurrentSkipListSet<Integer> javaSkipList = new ConcurrentSkipListSet<>();
        long startTime, endTime, totalTime;

        // Perform insert operation - Your SkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            mySkipList.add(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Insert Operation - Your SkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        // Perform insert operation - Java library's ConcurrentSkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            javaSkipList.add(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Insert Operation - Java library's ConcurrentSkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        // Perform contains operation - Your SkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            mySkipList.contains(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Contains Operation - Your SkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        // Perform contains operation - Java library's ConcurrentSkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            javaSkipList.contains(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Contains Operation - Java library's ConcurrentSkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        // Perform remove operation - Your SkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            mySkipList.remove(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Remove Operation - Your SkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        // Perform remove operation - Java library's ConcurrentSkipList
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            javaSkipList.remove(i);
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Remove Operation - Java library's ConcurrentSkipList - Threads: " + numThreads + ", Time: " + totalTime + " ms");

        System.out.println();
    }
}



class MySkipList<E extends Comparable<E>> {
    private Node<E> head;
    private int maxLevel;
    private int size;

    public MySkipList() {
        this.head = new Node<>(null, 0);
        this.maxLevel = 0;
        this.size = 0;
    }

    public void add(E element) {
        int level = getRandomLevel();
        /*
         This line generates a random level for the new node to be inserted. 
         The getRandomLevel method returns a level based on a probability of 0.5,
          which determines the height of the node in the skip list.
         */
        Node<E> newNode = new Node<>(element, level);
        // This creates a new node with the given element and the generated level.
        /*
         This condition checks if the newly generated level is greater than the current maximum level 
         of the skip list. If true, it means the new node extends beyond the current maximum level, 
         so the skip list needs to be extended as well.
         */
        if (level > maxLevel) {
            for (int i = maxLevel + 1; i <= level; i++) {
            /*
              Inside the above condition, this loop iterates from the current maximum level plus 
              one up to the new level. It ensures that for each level between the current maximum 
              and the new level, a null reference is added to the forwardNodes list of the head node. 
              This step effectively extends the skip list at those levels.
              
             */
                head.getForwardNodes().add(null);
            }
            maxLevel = level;
            //After the extension of the skip list, the maxLevel variable is updated to the new level.
        }

        Node<E> current = head;
        // This assigns the head node to the current variable, which will be used to traverse the skip list during 
        // the insertion process.
        for (int i = maxLevel; i >= 0; i--) {
            // This loop starts from the highest level (the maximum level of the skip list) and iterates down to level 0.

            while (current.getForwardNodes().size() > i && current.getForwardNodes().get(i) != null && current.getForwardNodes().get(i).getElement().compareTo(element) < 0) {
                /*
                  Inside the above loop, this condition checks if there is a forward node at the current 
                  level (current.getForwardNodes().size() > i), if that forward node is not null (current.getForwardNodes().get(i) != null), and 
                  if the element in that forward node is less than the element being inserted (current.getForwardNodes().get(i).getElement().compareTo(element) < 0).
                    This condition is used to find the correct position in the skip list where the new node should be inserted.
                    It searches for the appropriate location by comparing the elements of the forward nodes at each level with the given element.
                 */
                current = current.getForwardNodes().get(i);
            }

            if (i <= level) {
                /*
                  Inside the above loop, this condition checks if the current level is less than or equal to the new level. 
                  If true, it means the current level is within the range of the new node's levels, so the new node needs to 
                  be inserted at this level.
                 */
                if (current.getForwardNodes().size() > i)
                /*
                  This condition checks if the forward node list of the current node has a size greater than the current level.
                  It ensures that the list has enough elements to accommodate the new node at the current level.
                */
                    newNode.getForwardNodes().add(current.getForwardNodes().get(i));
                /*
                  This line adds the existing forward node at the current level (if any) to the forward 
                  nodes list of the new node.
                */
                else
                /*
                  If the condition in step if (current.getForwardNodes().size() > i) is false, it means 
                  the forward node list doesn't have an element at the current level. In this case, null is added 
                  to the forward nodes list of the new node.
                */
                    newNode.getForwardNodes().add(null);

                if (current.getForwardNodes().size() > i)
                /*
                  This condition is similar to if (current.getForwardNodes().size() > i). It checks if the 
                  forward node list of the current node has a size greater than the current level.
                 */
                    current.getForwardNodes().set(i, newNode);
                    // This line replaces the existing forward node at the current level (if any) with the 
                    //new node. It updates the forward node reference in the current node to point to the new node.
                else
                    current.getForwardNodes().add(newNode);
                    /*
                      If the condition in step 13 is false, it means the forward node list doesn't 
                      have an element at the current level. In this case, the new node is added to the forward 
                      nodes list of the current node.
                     */
            }
        }

        size++;
    }

    public void remove(E element) {
        Node<E> current = head;
        for (int i = maxLevel; i >= 0; i--) {
            while (current.getForwardNodes().size() > i && current.getForwardNodes().get(i) != null && current.getForwardNodes().get(i).getElement().compareTo(element) < 0) {
                current = current.getForwardNodes().get(i);
            }

            if (current.getForwardNodes().size() > i && current.getForwardNodes().get(i) != null && current.getForwardNodes().get(i).getElement().equals(element)) {
                current.getForwardNodes().set(i, current.getForwardNodes().get(i).getForwardNodes().get(i));
            }
        }

        size--;
    }

    public boolean contains(E element) {
        Node<E> current = head;
        for (int i = maxLevel; i >= 0; i--) {
            while (current.getForwardNodes().size() > i && current.getForwardNodes().get(i) != null && current.getForwardNodes().get(i).getElement().compareTo(element) < 0) {
                current = current.getForwardNodes().get(i);
            }

            if (current.getForwardNodes().size() > i && current.getForwardNodes().get(i) != null && current.getForwardNodes().get(i).getElement().equals(element)) {
                return true;
            }
        }

        return false;
    }

    private int getRandomLevel() {
        int level = 0;
        while (ThreadLocalRandom.current().nextDouble() < 0.5 && level < maxLevel + 1) {
            level++;
        }
        return level;
    }

    private static class Node<E> {
        private E element;
        private ArrayList<Node<E>> forwardNodes;

        public Node(E element, int level) {
            this.element = element;
            this.forwardNodes = new ArrayList<>(level + 1);
            for (int i = 0; i <= level; i++) {
                this.forwardNodes.add(null);
            }
        }

        public E getElement() {
            return element;
        }

        public ArrayList<Node<E>> getForwardNodes() {
            return forwardNodes;
        }
    }
}
