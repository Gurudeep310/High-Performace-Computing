#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

#define MAXN 16 // Varied Manually

int board[MAXN], count = 0;

int place(int row, int column) {
    int j;
    for (j = 0; j < row; j++) {
        if (board[j] == column || 
            board[j] - column == j - row || 
            board[j] - column == row - j)
            return 0;
    }
    return 1;
}

void nqueens(int row, int n) {
    int i, j;
    if (row == n) {
        #pragma omp critical
        {
            count++;
            printf("Solution %d: ", count);
            for (i = 0; i < n; i++)
                printf("%d ", board[i] + 1);
            printf("\n");
        }
    } else {
        #pragma omp parallel for private(i, j) num_threads(omp_get_num_threads())
        for (i = 0; i < n; i++) {
            if (place(row, i)) {
                board[row] = i;
                nqueens(row + 1, n);
            }
        }
    }
}

int main() {
    int n, i, num_threads;
    double start_time, end_time;
    double elapsed;
    n = 16; // Varied Manually Size of chess board
    int threads[9] = {1,2,4,6,8,10,12,14,16};
    for(int i = 0; i<9; i++){
        count = 0;
        int numTh = threads[i];
        for(int j = 0; j<5; j++){
            start_time = omp_get_wtime();
            #pragma omp parallel num_threads(numTh)
            {
                #pragma omp single nowait
                {
                    nqueens(0, n);
                }
            }
            end_time = omp_get_wtime();
            elapsed = elapsed + (start_time - end_time);
        }
        elapsed = elapsed/5;
        printf("Threads %d Time taken: %lf seconds\n",numTh,elapsed);
    }
    return 0;
}
