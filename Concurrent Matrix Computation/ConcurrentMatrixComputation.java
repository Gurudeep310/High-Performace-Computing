import java.util.concurrent.*;

public class ConcurrentMatrixComputation {
    private static final int m = 4096;
    private static final int n = 64;
    private static final int numThreads = 20;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads); // responsible for managing and executing the matrix computation tasks.
        CountDownLatch barrier = new CountDownLatch(numThreads); // ensure that the main thread waits until all the computation tasks are completed before proceeding.

        // Create the matrices X and I
        int[][] X = createMatrix(m);
        int[][] I = createUnitMatrix(m);

        // Divide the computation among threads
        int blockSize = m / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * blockSize;
            int endRow = startRow + blockSize;
            executor.submit(new MatrixComputationTask(X, I, startRow, endRow, barrier));
        }

        try {
            barrier.await(); // Wait for all threads to complete
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Compute the final result
        int[][] result = MatrixComputationTask.getResult();

        // Print the result matrix (example: print first 10x10 elements)
        printMatrix(result, 10, 10);
    }

    private static int[][] createMatrix(int size) {
        int[][] matrix = new int[size][size];
        // Initialize matrix elements with desired values
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = i + j; // Example: Set each element to the sum of its indices
            }
        }
        return matrix;
    }

    private static int[][] createUnitMatrix(int size) {
        int[][] unitMatrix = new int[size][size];
        // Create unit matrix with 1's on the diagonal and 0's elsewhere
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                unitMatrix[i][j] = (i == j) ? 1 : 0;
            }
        }
        return unitMatrix;
    }

    private static void printMatrix(int[][] matrix, int rows, int cols) {
        // Print the first 'rows' rows and 'cols' columns of the matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    static class MatrixComputationTask implements Runnable {
        private static final int[][] result = new int[m][m];
        private final int[][] X;
        private final int[][] I;
        private final int startRow;
        private final int endRow;
        private final CountDownLatch barrier;

        public MatrixComputationTask(int[][] X, int[][] I, int startRow, int endRow, CountDownLatch barrier) {
            this.X = X;
            this.I = I;
            this.startRow = startRow;
            this.endRow = endRow;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            // Perform the computation for the assigned rows
            long startTime = System.currentTimeMillis();
            for (int row = startRow; row < endRow; row++) {
                for (int col = 0; col < m; col++) {
                    result[row][col] = computeElement(X, I, row, col);
                }
            }
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            double throughput = (double) (m * (endRow - startRow)) / totalTime;

            System.out.println("Thread " + Thread.currentThread().getId() + ": Throughput = " + throughput);

            barrier.countDown();
        }

        private int computeElement(int[][] X, int[][] I, int row, int col) {
            int sum = 0;

            for (int i = n; i >= 2; i--) {
                sum += X[row][col] + I[row][col];
            }

            return sum;
        }

        public static int[][] getResult() {
            return result;
        }
    }
}
/*import java.util.concurrent.*;

public class ConcurrentMatrixComputation {
    private static final int m = 4096;
    private static final int n = 64;
    private static final int numThreads = 20;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch barrier = new CountDownLatch(numThreads);

        // Create the matrices X and I
        int[][] X = createMatrix(m);
        int[][] I = createUnitMatrix(m);

        // Divide the computation among threads
        int blockSize = m / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * blockSize;
            int endRow = startRow + blockSize;
            executor.submit(new MatrixComputationTask(X, I, startRow, endRow, barrier));
        }

        try {
            barrier.await(); // Wait for all threads to complete
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Compute the final result
        int[][] result = MatrixComputationTask.getResult();

        // Print the result matrix (example: print first 10x10 elements)
        printMatrix(result, 10, 10);
    }

    private static int[][] createMatrix(int size) {
        int[][] matrix = new int[size][size];
        // Initialize matrix elements with desired values
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = i + j; // Example: Set each element to the sum of its indices
            }
        }
        return matrix;
    }

    private static int[][] createUnitMatrix(int size) {
        int[][] unitMatrix = new int[size][size];
        // Create unit matrix with 1's on the diagonal and 0's elsewhere
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                unitMatrix[i][j] = (i == j) ? 1 : 0;
            }
        }
        return unitMatrix;
    }

    private static void printMatrix(int[][] matrix, int rows, int cols) {
        // Print the first 'rows' rows and 'cols' columns of the matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    static class MatrixComputationTask implements Runnable {
        private static final int[][] result = new int[m][m];
        private final int[][] X;
        private final int[][] I;
        private final int startRow;
        private final int endRow;
        private final CountDownLatch barrier;

        public MatrixComputationTask(int[][] X, int[][] I, int startRow, int endRow, CountDownLatch barrier) {
            this.X = X;
            this.I = I;
            this.startRow = startRow;
            this.endRow = endRow;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            // Perform the computation for the assigned rows
            for (int row = startRow; row < endRow; row++) {
                for (int col = 0; col < m; col++) {
                    result[row][col] = computeElement(X, I, row, col);
                }
            }

            barrier.countDown();
        }

        private int computeElement(int[][] X, int[][] I, int row, int col) {
            int sum = 0;

            for (int i = n; i >= 2; i--) {
                sum += X[row][col] + I[row][col];
            }

            return sum;
        }

        public static int[][] getResult() {
            return result;
        }
    }
}
*/