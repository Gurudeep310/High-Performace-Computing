#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <omp.h>

double** m;
double** r;
double** tmp;
double** temp;

int main()
{
	int size;
    struct timeval tv1, tv2;
    struct timezone tz;
    double elapsed;
    int size1[3] = {512,1024,2048};
    int N1[8] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    int threads1[9] = {1,2,4,6,8,10,12,14,16};
    for(int l = 0; l<sizeof(size1)/sizeof(size1[0]); l++){
    		int size = size1[l];
    		printf("Size %d\n",size);
		    m =(double **)calloc(sizeof(double *),size);
            r =(double **)calloc(sizeof(double *),size);
            tmp =(double **)calloc(sizeof(double *),size);
            temp = (double **)calloc(sizeof(double *),size);
            for(int o = 0;o<size; o++){
                m[o] = (double*)calloc(sizeof(double),size);
                r[o] = (double*)calloc(sizeof(double),size);
                tmp[o] = (double*)calloc(sizeof(double),size);
                temp[o] = (double*)calloc(sizeof(double),size);
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
                        m[i][j]=(rand() %(2));
                        r[i][j]=m[i][j];
                        
                    }
                }
                for (int i = 0; i < size; i++){
                    for (int j = 0; j < size; j++){
                        temp[i][j] = m[j][i];
                    }
                }
                for (int i = 0; i < size; i++){
                    for (int j = 0; j < size; j++){
                        r[i][j] = temp[i][j];
                    }
                }
                int sum=0;
                //-----------
                int i = 0;
                int j = 0;
                int k = 0;
                int l = 0;
                int s = 0;
                int numreps = 0;
                int N1[8] = {2,4,6,8,10,12,14,16};
			    #pragma omp parallel for private(i,j,k,l,s,numreps) shared(m,r,tmp)
			    for(s = 0;s<sizeof(N1)/sizeof(N1[0]);s++){
				    int N = N1[s];
                    for(numreps = 0; numreps<5; numreps++){
                        for (i=0;i<N-1;i++){
                            gettimeofday(&tv1, &tz);
                            for(j=0;j<size;j++){
                                for(k=0;k<size;k++){
                                    for(l=0;l<size;l++)
                                        sum=sum+(r[j][l]*m[l][k]);
                                    tmp[j][k]=sum;		
                                    sum=0;	
                                }	
                                
                            }
                            
                            for(i=0;i<size;i++){
                                for(j=0;j<size;j++){
                                    r[i][j]=tmp[i][j];
                                }
                            }
                            gettimeofday(&tv2, &tz); 
                        }
				        elapsed += (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec) * 1.e-6;
                    }
				    printf("size %d thread %d power %d avg_elapsed %4.2lf\n",size,numTh,N,elapsed);
                }
			}
		}
}