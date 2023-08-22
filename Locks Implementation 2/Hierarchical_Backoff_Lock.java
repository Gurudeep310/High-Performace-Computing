import java.util.concurrent.atomic.AtomicInteger;

public class Hierarchical_Backoff_Lock {
    static AtomicInteger counter = new AtomicInteger(0);
    static volatile int[] lockArray = new int[128];
    static ThreadLocal<Integer> mySlot = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static void hb_lock() {
        int myIndex = mySlot.get();
        int backoff = 1;

        while (true) {
            // Acquire all locks from root to leaf
            for (int i = 0; i <= myIndex; i++) {
                lockArray[i] = 1;
            }

            // Check if all locks are successfully acquired
            boolean acquired = true;
            for (int i = 0; i <= myIndex; i++) {
                if (lockArray[i] == 0) {
                    acquired = false;
                    break;
                }
            }

            if (acquired) {
                return;
            }

            // Release acquired locks
            for (int i = 0; i <= myIndex; i++) {
                lockArray[i] = 0;
            }

            // Backoff
            try {
                Thread.sleep((int)(Math.random() * backoff));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Increase backoff factor
            backoff *= 2;
        }
    }

    public static void hb_unlock() {
        int myIndex = mySlot.get();

        // Release all locks from leaf to root
        for (int i = myIndex; i >= 0; i--) {
            lockArray[i] = 0;
        }

        // Update thread's slot to its parent
        mySlot.set((myIndex - 1 + 128) % 128);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: HBLock <num_threads> <time_ms>");
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
                        hb_lock();
                        counter.incrementAndGet();
                        hb_unlock();
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

        System.out.println("Counter value: " + counter.get() + ", Numthread: " + num_threads + ", totalOps: " + counter.get());
    }
}

