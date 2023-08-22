#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <sys/time.h>

double **A;
double **B;
double **C;

int main(int argc, char*argv[]) 
{
	int i,j,k;
	struct timeval tv1, tv2;
	struct timezone tz;
	double elapsed; 
	int no_of_threads[10] = {1,2,4,6,8,10,12,14,16};
	int size=atoi(argv[1]);
	int N=size;
	for(int g = 0; g<9; g++){
		int numTh=no_of_threads[g];
		double sum=0.0;
		//printf("Size is %d",N);
		double mean = 0.0;
		for(int l = 0; l < 5; l++){
			A=(double **)calloc(sizeof(double *), size);
			B=(double **)calloc(sizeof(double *), size);
			C=(double **)calloc(sizeof(double *), size);
			for(i=0;i<size;i++)
			{
				A[i]=(double *)calloc(sizeof(double), size);
				B[i]=(double *)calloc(sizeof(double), size);
				C[i]=(double *)calloc(sizeof(double), size);
			}
			omp_set_num_threads(numTh);
			for (i= 0; i< N; i++)
			for (j= 0; j< N; j++)
			{
				A[i][j] = 2;
				B[i][j] = 2;
			}
			//printf("Number of Threads: %d",omp_get_num_procs());
			gettimeofday(&tv1, &tz);
			#pragma omp parallel for private(i,j,k) shared(A,B,C)
			for (i = 0; i < N; ++i) {
				for (j = 0; j < N; ++j) {
					for (k = 0; k < N; ++k) { C[i][j]+=A[i][k] * B[k][j];  }
				}
			}
			gettimeofday(&tv2, &tz);

			elapsed = elapsed + (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec)* 1.e-6;
		}
		mean = elapsed/5;
		//printf("elapsed time = %4.2lf seconds.\n", elapsed);
		printf("%4.2lf %d\n",mean, numTh);
		/*for (i= 0; i< N; i++)  { 
			for (j= 0; j< N; j++) {  
				printf("%lf\t",C[i][j]); 
			} 
		printf("\n"); 
		}*/
	}
}
