#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <time.h>

#define MIN(a,b) (((a)<(b))?(a):(b))
#define MAX(a,b) (((a)>(b))?(a):(b))

int main(int argc, char *argv[]) {

    if(argc != 6) {
        printf("Usage: ./threshold r c <file_name> p <output_file_name>\n");
        exit(1);
    }

    int r = atoi(argv[1]);
    int c = atoi(argv[2]);
    char *input_file = argv[3];
    int p = atoi(argv[4]);
    char *output_file = argv[5];

    int **M = (int **)malloc(r * sizeof(int *));
    for(int i = 0; i < r; i++) {
        M[i] = (int *)malloc(c * sizeof(int));
    }

    int **B = (int **)malloc(r * sizeof(int *));
    for(int i = 0; i < r; i++) {
        B[i] = (int *)malloc(c * sizeof(int));
    }

    srand(time(NULL)); // initialize random number generator
    FILE *fp1;
    fp1 = fopen(input_file, "w");
    for(int i = 0; i < r; i++) {
        for(int j = 0; j < c; j++) {
            int val = rand() % 301; // generate random value between 0 to 300
            fprintf(fp1, "%d ", val);
        }
        fprintf(fp1, "\n");
    }
    fclose(fp1);

    FILE *fp;
    fp = fopen(input_file, "r");
    for(int i = 0; i < r; i++) {
        for(int j = 0; j < c; j++) {
            fscanf(fp, "%d", &M[i][j]);
            M[i][j] = MAX(0, MIN(M[i][j], 300));
        }
    }
    fclose(fp);

    for(int num_threads = 1; num_threads <= 16; num_threads++) {
        double start_time = omp_get_wtime();

        #pragma omp parallel for shared(M, B, p) num_threads(num_threads)
        for(int i = 0; i < r; i++) {
            for(int j = 0; j < c; j++) {
                int count = 0;
                for(int x = i-1; x <= i+1; x++) {
                    for(int y = j-1; y <= j+1; y++) {
                        if(x >= 0 && x < r && y >= 0 && y < c) {
                            if(M[x][y] > M[i][j]) {
                                count++;
                            }
                        }
                    }
                }
                if(count > p/100.0 * 9) {
                    B[i][j] = 1;
                }
                else {
                    B[i][j] = 0;
                }
            }
        }

        double end_time = omp_get_wtime();

        fp = fopen(output_file, "w");
        for(int i = 0; i < r; i++) {
            for(int j = 0; j < c; j++) {
                fprintf(fp, "%d ", B[i][j]);
            }
            fprintf(fp, "\n");
        }
        fclose(fp);

        printf("Execution time with %d threads: %f\n", num_threads, end_time - start_time);
    }

    return 0;
}

