import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MCSLock {
    static final int CAPACITY = 100;
    static AtomicInteger counter = new AtomicInteger(0);
    static AtomicReference<QNode> tail = new AtomicReference<>(null);
    static ThreadLocal<QNode> myNode = ThreadLocal.withInitial(QNode::new);

    public static void lock() {
        QNode node = myNode.get();
        QNode pred = tail.getAndSet(node);

        if (pred != null) {
            node.locked = true;
            pred.next = node;

            while (node.locked) {
                // spin until lock is acquired
            }
        }
    }

    public static void unlock() {
        QNode node = myNode.get();

        if (node.next == null) {
            if (tail.compareAndSet(node, null)) {
                // No other threads are waiting, lock released
                return;
            }

            // Wait until the next thread is set
            while (node.next == null) {
                // spin until the next thread is set
            }
        }

        node.next.locked = false;
        node.next = null;
    }

    private static class QNode {
        volatile boolean locked = false;
        QNode next = null;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: Main <num_threads> <time_ms>");
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
