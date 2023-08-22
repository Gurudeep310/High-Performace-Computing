#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <time.h>
#include <sys/time.h>

#define ROWS 10
#define COLS 10
#define ITERATIONS 10

int board[ROWS][COLS];
int newboard[ROWS][COLS];

// Function to update the state of a cell based on its neighbors
int updateC(int i, int j) {
    int neighbors = 0;
    for (int row = i - 1; row <= i + 1; row++) {
        for (int col = j - 1; col <= j + 1; col++) {
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (row != i || col != j) {
                    neighbors += board[row][col];
                }
            }
        }
    }
    if (board[i][j] == 1) {
        if (neighbors < 2 || neighbors > 3) {
            return 0;
        } else {
            return 1;
        }
    } else {
        if (neighbors == 3) {
            return 1;
        } else {
            return 0;
        }
    }
}

int main(int argc, char** argv) {
    int threads[9] = {1,2,4,6,8,10,12,14,16};
	struct timeval tv1, tv2;
	struct timezone tz;
	double elapsed; 
    for(int g = 0; g<9; g++){
        int numThreads = threads[g];
        omp_set_num_threads(numThreads);
        int s;
        // Initialize the grid randomly with no more than 10% live cells
        do {
            int numLiveCells = 0;
            #pragma omp parallel for num_threads(numThreads) reduction(+:numLiveCells)
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    board[i][j] = rand() % 2;
                    numLiveCells += board[i][j];
                }
            }
            double percentLiveCells = 100.0 * (double) numLiveCells / (ROWS * COLS);
            if (percentLiveCells <= 10.0) {
                break;
            }
        } while (1);
        // Main loop of the algorithm
        for(s = 0; s<5; s++){
            gettimeofday(&tv1, &tz);
            #pragma omp parallel for num_threads(numThreads)
            for (int iter = 0; iter < ITERATIONS; iter++) {
                // Update the state of the cells in parallel
                #pragma omp parallel for num_threads(numThreads)
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        newboard[i][j] = updateC(i, j);
                    }
                }
                // Copy the new state of the cells back to the grid
                #pragma omp parallel for num_threads(numThreads)
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        board[i][j] = newboard[i][j];
                    }
                }
            }
            gettimeofday(&tv2, &tz);
            elapsed = elapsed + (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec) * 1.e-6;
        }
        printf("Threads %d elapsed time = %4.2lf seconds.\n", numThreads, elapsed);
        elapsed = 0;
    }
    return 0;
}