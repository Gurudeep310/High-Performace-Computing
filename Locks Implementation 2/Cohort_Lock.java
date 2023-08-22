// Working only for one thread
import java.util.concurrent.atomic.AtomicInteger;

public class Cohort_Lock{
    private static final int MAX_THREADS = 32; // Maximum number of threads supported by the lock
    private static final int COHORT_SIZE = 4;  // Cohort size for the lock
    private static final int NUM_COHORTS = MAX_THREADS / COHORT_SIZE; // Number of cohorts

    private static AtomicInteger counter = new AtomicInteger(0);
    private static boolean[] flags = new boolean[MAX_THREADS];
    private static int[] cohorts = new int[NUM_COHORTS];
    private static ThreadLocal<Integer> myIndex = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return -1;
        }
    };

    static {
        for (int i = 0; i < NUM_COHORTS; i++) {
            cohorts[i] = -1;
        }
    }

    public static void cohort_lock(int threadId) {
        int cohortId = threadId / COHORT_SIZE;
        myIndex.set(threadId % COHORT_SIZE);

        while (true) {
            flags[threadId] = true;
            if (cohorts[cohortId] == -1) {
                cohorts[cohortId] = myIndex.get();
                while (true) {
                    boolean ok = true;
                    for (int i = 0; i < COHORT_SIZE; i++) {
                        if (flags[cohortId * COHORT_SIZE + i] && cohorts[cohortId] > i) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        return;
                    }
                }
            }
        }
    }

    public static void cohort_unlock(int threadId) {
        int cohortId = threadId / COHORT_SIZE;
        flags[threadId] = false;
        if (myIndex.get() == cohorts[cohortId]) {
            cohorts[cohortId] = -1;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: CohortLock <num_threads> <time_ms>");
            System.exit(1);
        }

        int num_threads = Integer.parseInt(args[0]);
        int time_ms = Integer.parseInt(args[1]);
        Thread[] threads = new Thread[num_threads];
        long start_time = System.currentTimeMillis();

        for (int i = 0; i < num_threads; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int threadId = Integer.parseInt(Thread.currentThread().getName());
                    long start_time = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - start_time) < time_ms) {
                        cohort_lock(threadId);
                        counter.incrementAndGet(); // incrementing the count value in critical section
                        cohort_unlock(threadId);
                    }
                }
            }, Integer.toString(i));
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
