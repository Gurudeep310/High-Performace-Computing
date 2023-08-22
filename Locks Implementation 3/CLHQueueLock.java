import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class CLHQueueLock {
    private final AtomicReference<QNode> tail;
    private final ThreadLocal<QNode> myPred;
    private final ThreadLocal<QNode> myNode;

    public CLHQueueLock() {
        this.tail = new AtomicReference<>(new QNode());
        this.myPred = ThreadLocal.withInitial(() -> null);
        this.myNode = ThreadLocal.withInitial(QNode::new);
    }

    public void lock() {
        QNode node = myNode.get();
        node.locked = true;
        QNode pred = tail.getAndSet(node);
        myPred.set(pred);
        while (pred.locked) {
            // spin
        }
    }

    public void unlock() {
        QNode node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }

    private static class QNode {
        volatile boolean locked;
    }

    public static void main(String[] args) {
        final int NUM_THREADS = 4;
        final int NUM_TASKS = 1000;
        final CLHQueueLock lock = new CLHQueueLock();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_TASKS; i++) {
            executor.submit(new Task(lock));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // wait for all tasks to complete
        }

        int expected = NUM_TASKS;
        int actual = Task.counter.get();

        if (actual != expected) {
            System.err.println("Counter is incorrect: expected " + expected + ", actual " + actual);
        } else {
            System.out.println("Counter is correct: " + actual);
        }
    }

    private static class Task implements Runnable {
        private static AtomicInteger counter = new AtomicInteger(0);
        private final CLHQueueLock lock;

        public Task(CLHQueueLock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            lock.lock();
            try {
                counter.incrementAndGet();
            } finally {
                lock.unlock();
            }
        }
    }
}
