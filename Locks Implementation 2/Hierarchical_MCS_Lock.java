import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Hierarchical_MCS_Lock {
    static AtomicInteger counter = new AtomicInteger(0);
    static AtomicReference<QNode> tail = new AtomicReference<>(null);
    static ThreadLocal<QNode> myNode = new ThreadLocal<>();
    
    private static class QNode {
        volatile boolean locked = false;
        volatile QNode parent = null;
    }
    
    public static void lock() {
        QNode node = new QNode();
        myNode.set(node);
        QNode pred = tail.getAndSet(node);
        if (pred != null) {
            node.locked = true;
            node.parent = pred;
            pred.parent = node;
            while (node.locked) {
                // spin until the node's parent unlocks it
            }
        }
    }
    
    public static void unlock() {
        QNode node = myNode.get();
        if (node.parent == null) {
            if (tail.compareAndSet(node, null)) {
                return;
            }
            while (node.parent == null) {
                // spin until the tail is updated
            }
        }
        node.parent.locked = false;
        myNode.set(node.parent);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: HierarchicalMCSLock <num_threads> <time_ms>");
            System.exit(1);
        }
    
        int num_threads = Integer.parseInt(args[0]);
        int time_ms = Integer.parseInt(args[1]);
        Thread[] threads = new Thread[num_threads];
        long start_time = System.currentTimeMillis();
    
        for (int i = 0; i < num_threads; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    long start_time = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - start_time) < time_ms) {
                        lock();
                        counter.incrementAndGet();
                        unlock();
                    }
                }
            });
            threads[i].start();
        }
    
        for (int i = 0; i < num_threads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        System.out.println("Counter value: " + counter.get() + ", NumThreads: " + num_threads + ", TotalOps: " + counter.get());
    }
}

