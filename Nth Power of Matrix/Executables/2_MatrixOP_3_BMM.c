#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <omp.h>

double** A;
double** B;
double** C;

void Multiply(int n, double** a, double** b, double** c, int blockSize)
{
    int bi=0;
    int bj=0;
    int bk=0;
    int i=0;
    int j=0;
    int k=0; 

    //#pragma omp parallel for private(i,j,k) shared(A,B,C)
    for(bi=0; bi<n; bi+=blockSize)
        for(bj=0; bj<n; bj+=blockSize)
            for(bk=0; bk<n; bk+=blockSize)
                for(i=0; i<blockSize; i++)
                    for(j=0; j<blockSize; j++)
                        for(k=0; k<blockSize; k++)
                            c[bi+i][bj+j] += a[bi+i][bk+k]*b[bk+k][bj+j];
}
int main(void)
{
    int numreps = 5;
    int i=0;
    int j=0;
    int k = 0;
    int l = 0;
    int s = 0;
    int block = 0;
    struct timeval tv1, tv2;
    struct timezone tz;
    double elapsed;
    int thread1[9] = {1,2,4,6,8,10,12,14,16};
    int size[3] = {512,1024,2048};
    int blockSize[5] = {4,8,16,32,64};
    int power[16] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    //printf ("Please enter matrix dimension n : ");
    //scanf("%d", &size);
    for(int z = 0; z<sizeof(size)/sizeof(size[0]); z++){
        int n = size[z];
        // allocate memory for the matrices 
        A =(double **)calloc(sizeof(double *),n);
        B =(double **)calloc(sizeof(double *),n);
        C =(double **)calloc(sizeof(double *),n);
        for(i = 0;i<n; i++)
        {
            A[i] = (double*)calloc(sizeof(double),n);
            B[i] = (double*)calloc(sizeof(double),n);
            C[i] = (double*)calloc(sizeof(double),n);
        }
        for(l = 0; l<sizeof(thread1)/sizeof(thread1[0]);l++){
        	int numTh = thread1[l];
		omp_set_num_threads(numTh);
		// initialize the matrices
		for(i=0; i<n; i++)
		{
		    for(j=0; j<n; j++)
		    {
		        A[i][j] = (double)rand() / RAND_MAX;
		        B[i][j] = (double)rand() / RAND_MAX;
		    }
		}
		//multiply matrices
		printf("Multiply matrices %d times...\n", n);
		for(int y = 0; y<sizeof(blockSize)/sizeof(blockSize[0]); y++){	
			block = blockSize[y];
			for(int z = 0; z<sizeof(power)/sizeof(power[0]);z++){
				int pow = power[z];
				gettimeofday(&tv1, &tz);
				for(s = 0; s<pow-1;s++){
				    Multiply(n,A,B,C,block);
				}
				gettimeofday(&tv2, &tz);
				elapsed = (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec) * 1.e-6;
				printf("size %d block %d thread %d power %d elapsed time = %4.2lf seconds.\n", n,block,numTh,pow,elapsed);
			}
		}
	}
    }
    return 0;
}
