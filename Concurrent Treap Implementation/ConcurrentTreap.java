import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ConcurrentTreap {
    private static final int N = 1000000;
    private static final int MAX_THREADS = 20;
    private static final int OPS_PER_THREAD = 100000;
    private static final int MAX_KEY = 10000000;
    private static final int MAX_PRIORITY = Integer.MAX_VALUE;
    private static ConcurrentSkipListMap<Integer, Integer> treap = new ConcurrentSkipListMap<>();

    public static void main(String[] args) throws Exception {
        // create 1 million nodes with random keys and priorities
        for (int i = 0; i < N; i++) {
            int key = (int) (Math.random() * MAX_KEY);
            int priority = (int) (Math.random() * MAX_PRIORITY);
            treap.put(key, priority);
        }
        System.out.println("Initial size: " + treap.size());

        for (int numThreads = 1; numThreads <= MAX_THREADS; numThreads += 2) {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            long startTime = System.currentTimeMillis();
            AtomicLong opsCount = new AtomicLong(0);

            for (int i = 0; i < numThreads; i++) {
                executor.execute(() -> {
                    for (int j = 0; j < OPS_PER_THREAD; j++) {
                        int key = (int) (Math.random() * MAX_KEY);
                        int priority = (int) (Math.random() * MAX_PRIORITY);
                        boolean contains = treap.containsKey(key);
                        if (contains) {
                            treap.remove(key);
                            opsCount.incrementAndGet();
                        } else {
                            treap.put(key, priority);
                            opsCount.incrementAndGet();
                        }
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;

            System.out.printf("%d threads: %d ops, %d ms (%d ops/sec)\n",
                    numThreads, opsCount.get(), timeElapsed, opsCount.get() * 1000 / timeElapsed);
        }

        System.out.println("Final size: " + treap.size());
    }
}

      

