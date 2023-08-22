#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

#define N 10

int main()
{
    int p[N+1] = {1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288};
    int n = N;
    int i, j, k, l, x;
    double start, end;
    double **a, **b, **c;

    // Allocate memory for matrices
    a = (double **) malloc(n * sizeof(double *));
    b = (double **) malloc(n * sizeof(double *));
    c = (double **) malloc(n * sizeof(double *));
    for (i = 0; i < n; i++) {
        a[i] = (double *) malloc(p[i] * sizeof(double));
        b[i] = (double *) malloc(p[i+1] * sizeof(double));
        c[i] = (double *) malloc(p[i] * p[i+1] * sizeof(double));
    }

    // Initialize matrices
    for (i = 0; i < n; i++) {
        for (j = 0; j < p[i]; j++) {
            a[i][j] = j + 1;
        }
        for (j = 0; j < p[i+1]; j++) {
            b[i][j] = j + 1;
        }
    }

    printf("Threads\t Time\n");
    int count = 2;
    int count1 = 2;
    int threads[9] = {1,2,4,6,8,10,12,14,16};
    for (x = 0; x < 9; x = x+1) {
        // Set the number of threads
        omp_set_num_threads(threads[x]);

        // Matrix multiplication
        start = omp_get_wtime();

        #pragma omp parallel for private(i, j, k, l)
        for (l = 1; l < n; l++) {
            for (i = 0; i < n-l; i++) {
                j = i + l;
                for (k = i; k < j; k++) {
                    for (int m = 0; m < p[i]; m++) {
                        for (int n = 0; n < p[k+1]; n++) {
                            c[i][j*p[i+1]+n] += a[k][m] * b[k][n];
                        }
                    }
                }
            }
        }
        end = omp_get_wtime();
        printf("%d\t %f\n", threads[x], end-start);
    }

    // Free memory
    for (i = 0; i < n; i++) {
        free(a[i]);
        free(b[i]);
        free(c[i]);
    }
    free(a);
    free(b);
    free(c);

    return 0;
}
