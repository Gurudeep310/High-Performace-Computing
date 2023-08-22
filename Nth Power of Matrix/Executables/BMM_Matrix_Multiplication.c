#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <omp.h>

double** m;
double** r;
double** tmp;
// BMM Matrix Multiplication

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


int main()
{
	int size;
    	struct timeval tv1, tv2;
    	struct timezone tz;
    	double elapsed;
    	int size1[3] = {512,1024,2048};
        int blockSize[5] = {4,8,16,32,64};
    	int threads1[9] = {1,2,4,6,8,10,12,14,16};
    	for(int l = 0; l<sizeof(size1)/sizeof(size1[0]); l++)
    	{
    		int size = size1[l];
    		printf("Size %d\n",size);
		m =(double **)calloc(sizeof(double *),size);
		r =(double **)calloc(sizeof(double *),size);
		tmp =(double **)calloc(sizeof(double *),size);
		for(int o = 0;o<size; o++)
		{
		    m[o] = (double*)calloc(sizeof(double),size);
		    r[o] = (double*)calloc(sizeof(double),size);
		    tmp[o] = (double*)calloc(sizeof(double),size);
		}
		for(int n= 0;n<sizeof(threads1)/sizeof(threads1[0]);n++){
			int numTh = threads1[n];
			printf("numthread %d\n",numTh);
			omp_set_num_threads(numTh);
			
			// Initialise matrices
			for(int i=0;i<size;i++)
			{
				for(int j=0;j<size;j++)
				{
					m[i][j]=(double)rand() / RAND_MAX;
					r[i][j]=m[i][j];
					
				}
			}
			int sum=0;
			gettimeofday(&tv1, &tz);
			//-----------
			int i = 0;
			int j = 0;
			int k = 0;
			int l = 0;
			int s = 0;
			int N1[16] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
			#pragma omp parallel for private(i,j,k,l,s) shared(m,r,tmp)
			for(s = 0;s<sizeof(N1)/sizeof(N1[0]);s++)
			{
                int N = N1[s];
                for (i=0;i<N-1;i++){
                    for(int y = 0; y<sizeof(blockSize)/sizeof(blockSize[0]); y++){
                        gettimeofday(&tv1, &tz);
                        int block = blockSize[y];
                        Multiply(n,m,r,tmp,block);
                        gettimeofday(&tv2, &tz);
                        elapsed = (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec) * 1.e-6;
                        printf("size %d thread %d power %d for block %d elapsed %4.2lf\n",size,numTh,N,block,elapsed);
                    }    
                }
			}
		}
    }
}
