import java.util.concurrent.atomic.AtomicInteger;

public class AQueue{
    // Initialize atomic counters and queue size
    static AtomicInteger counter = new AtomicInteger(0);
    static AtomicInteger tail = new AtomicInteger(0);
    static AtomicInteger head = new AtomicInteger(0);
    static final int CAPACITY = 100;
    // Initialize queue
    static int[] queue = new int[CAPACITY];

    // Queue lock method
    public static void queue_lock() {
        // Get index of next available slot in the queue and increment tail counter
        int myIndex = tail.getAndIncrement() % CAPACITY;
        // Spin until the queue slot is empty
        while (queue[myIndex] == 1) {
            // Spin until queue slot is empty
        }
        // Set queue slot to indicate that it's now occupied
        queue[myIndex] = 1;
        // Spin until it's our turn to proceed
        while (head.get() != myIndex) {
            // Spin until it's our turn
        }
    }

    // Queue unlock method
    public static void queue_unlock() {
        // Get current queue head index
        int myIndex = head.get();
        // Release lock by setting the queue slot to empty
        queue[myIndex] = 0;
        // Move the head pointer to the next slot in the queue
        head.compareAndSet(myIndex, (myIndex + 1) % CAPACITY);
    }

    public static void main(String[] args) {
        // Check if command line arguments are valid
        if (args.length < 2) {
            System.err.println("Usage: Main <num_threads> <time_ms>");
            System.exit(1);
        }
        // Get number of threads and time in ms from command line arguments
        int num_threads = Integer.parseInt(args[0]);
        int time_ms = Integer.parseInt(args[1]);
        // Create thread array and start time
        Thread[] threads = new Thread[num_threads];
        long start_time = System.currentTimeMillis();

        // Spawn threads and execute critical section
        for (int i = 0; i < num_threads; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    long start_time = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - start_time) < time_ms) {
                        queue_lock();
                        counter.incrementAndGet();
                        queue_unlock();
                    }
                }
            });
            threads[i].start();
        }

        // Wait for threads to finish executing
        for (int i = 0; i < num_threads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Print final counter value and total number of operations performed
        System.out.println("Counter value: " + counter.get() + ", NumThreads: " + num_threads + ", TotalOps: " + counter.get());
    }
}

