import java.util.concurrent.atomic.AtomicBoolean; //provides atomic operations on boolean values.

public class CounterProgram {
    public static class TASLock { // This class represents the Test And Set lock.
        AtomicBoolean state = new AtomicBoolean(false);
        //This line creates an AtomicBoolean object named state and initializes it with false. 
        //The state variable represents the lock state, where false means the lock is available, 
        //and true means the lock is acquired by a thread.

        /*
         This method is used to acquire the lock. 
         It uses a busy-wait loop to continuously attempt to set the state to true using the getAndSet(true) method of AtomicBoolean. 
         The getAndSet(true) method atomically sets the state to true and returns its previous value.
         So as long as the previous value was true, indicating that the lock is already acquired, the loop continues to spin.
         */
        public void lock() {
            while (state.getAndSet(true)) {
            }
        }
        /*
         This method is used to release the lock. 
         It simply sets the state back to false, indicating that the lock is available again.
         */
        public void unlock() {
            state.set(false);
        }
    }

    public static class Counter {
        /*
         This code defines a nested static class named Counter within the CounterProgram class. 
         It represents a counter object that holds an integer value. The value variable holds the actual counter value, 
         and the lock variable represents the TAS lock object used for thread synchronization.
         */
        private int value = 0;
        private TASLock lock = new TASLock();

        /*
         This method is responsible for incrementing the counter value. 
         It first acquires the lock by calling lock.lock() to ensure exclusive access to the counter. 
         It then increments the value variable by one.
         Finally, in a finally block, it releases the lock by calling lock.unlock() to allow other threads to access the counter.
         */
        public void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }
        //This method simply returns the current value of the counter.
        public int getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        /*
         The main method is the entry point of the program. 
         It starts by initializing some variables. 
         numThreads represents the number of threads to create, and timeSec is the duration in seconds for which the threads will increment the counter. 
         The Counter object is instantiated, and an array of Thread objects is created to hold the threads. 
         startTime captures the current system time.
         */
        int numThreads = 10;
        int timeSec = Integer.parseInt(args[0]);
        Counter counter = new Counter();
        Thread[] threads = new Thread[numThreads];
        long startTime = System.currentTimeMillis();

        /*
         This code defines a Runnable named incrementCounter. 
         It is a lambda expression that represents the task to be performed by each thread. 
         The task is to continuously increment the counter while the elapsed time is less than the specified timeSec multiplied by 1000 (to convert it to milliseconds).
         */
        Runnable incrementCounter = () -> {
            while (System.currentTimeMillis() - startTime < timeSec * 1000) {
                counter.increment();
            }
        };
        /*
         This loop creates numThreads threads and assigns the incrementCounter task to each thread. The threads are then started.
         */
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(incrementCounter);
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Counter value: " + counter.getValue());
    }
}
