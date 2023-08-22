import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ExponentialBackoffLock {
    static AtomicInteger counter = new AtomicInteger(0);
    static AtomicInteger lock = new AtomicInteger(0);
    static final int MIN_DELAY = 1000;
    static final int MAX_DELAY = 10000;
    static Random random = new Random();

    public static void exponential_backoff_lock() {
        int delay = MIN_DELAY;
        while (true) {
            while (lock.get() == 1) {} // spin until lock is released
            if (lock.compareAndSet(0, 1)) {
                return; // lock acquired
            }
            // Exponential backoff with random delay between MIN_DELAY and MAX_DELAY
            if(delay<MAX_DELAY){
                delay = 2 * delay;
            }
            try {
                Thread.sleep(random.nextInt(delay));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exponential_backoff_unlock() {
        lock.set(0); // release the lock
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
                        exponential_backoff_lock();
                        counter.incrementAndGet();
                        exponential_backoff_unlock();
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

        long elapsed_time = time_ms;
        double throughput = (counter.get() / (elapsed_time / 1000.0)) / 1000000.0; // MOPS
        System.out.println("Counter value: " + counter.get() + ", NumThreads: " + num_threads + ", TotalOps: " + counter.get() + ", ElapsedTime(ms): " + elapsed_time + ", Throughput(MOPS): " + throughput);
    }
}
