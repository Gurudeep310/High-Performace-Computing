import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Queue_Lock_with_Timeout{
private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
private final AtomicInteger counter = new AtomicInteger(0);
private final AtomicInteger lock = new AtomicInteger(0);
public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
    long nanosTimeout = unit.toNanos(timeout);
    int myIndex = queue.size();
    queue.add(myIndex);

    long deadline = System.nanoTime() + nanosTimeout;
    while (System.nanoTime() < deadline) {
        if (myIndex == queue.peek() && lock.compareAndSet(0, 1)) {
            queue.poll();
            return true;
        }
        Thread.yield();
    }

    queue.remove(myIndex);
    return false;
}

public void unlock() {
    lock.set(0);
}

public static void main(String[] args) throws InterruptedException {
    if (args.length < 2) {
        System.err.println("Usage: QueueLockWithTimeout <num_threads> <time_ms>");
        System.exit(1);
    }

    int num_threads = Integer.parseInt(args[0]);
    int time_ms = Integer.parseInt(args[1]);

    Thread[] threads = new Thread[num_threads];
    Queue_Lock_with_Timeout lock = new Queue_Lock_with_Timeout();

    long start_time = System.currentTimeMillis();

    for (int i = 0; i < num_threads; i++) {
        threads[i] = new Thread(new Runnable() {
            public void run() {
                long start_time = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start_time) < time_ms) {
                    try {
                        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                            try {
                                lock.counter.incrementAndGet(); // increment the counter in critical section
                                Thread.yield();
                            } finally {
                                lock.unlock();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    System.out.println("Counter value: " + lock.counter.get() + ", NumThreads: " + num_threads + ", TotalOps: " + lock.counter.get());
}
}
