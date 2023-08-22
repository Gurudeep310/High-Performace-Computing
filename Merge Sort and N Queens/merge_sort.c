#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <omp.h>
#include <sys/time.h>

#define ARRAY_SIZE 40000000

void merge_sort(double *array, double *tmp, int size);
void merge_r(double *src, double *tmp, int start, int mid, int end);
void ins_sort(double *src, int start, int end);

int main() {
	struct timeval tv1, tv2;
	struct timezone tz;
	double elapsed; 
  int j = 0;
  int i;
  double *src, *tmp;
   src=(double *)malloc(ARRAY_SIZE * sizeof(double));
   tmp=(double *)malloc(ARRAY_SIZE * sizeof(double));

  srand(time(NULL));
  
  // generate random numbers for int type array
  for (i = 0; i < ARRAY_SIZE; i++) {
    src[i] = rand();
  }
/*
printf("before sorting\n");
for (i = 0; i < ARRAY_SIZE; i++) {
    printf("%d \t",src[i]);	
  }
printf("\n");*/
  int threads[9] = {1,2,4,6,8,10,12,14,16};
  for(int i = 0; i<9; i++){
    int numTh = threads[i];
    omp_set_num_threads(numTh);
    #pragma omp parallel for private(j) shared(src,tmp)
    for(j = 0; j<5;j++){
      gettimeofday(&tv1, &tz);
      // sort int type array
      merge_sort(src, tmp, ARRAY_SIZE);
      gettimeofday(&tv2, &tz);
      elapsed = elapsed + (double) (tv2.tv_sec-tv1.tv_sec) + (double) (tv2.tv_usec-tv1.tv_usec) * 1.e-6;
    }
    //elapsed = elapsed/5;
    printf("Threads %d elapsed time = %4.2lf seconds.\n", numTh, elapsed);
    elapsed = 0;
  }

/*printf("after sorting\n");
for (i = 0; i < ARRAY_SIZE; i++) {
    printf("%d \t",src[i]);
	
  }
printf("\n");*/
  for(i=1;i<ARRAY_SIZE&&(src[i-1]<=src[i]);i++);
  if(i==ARRAY_SIZE) { printf("Test passed");}
  else { printf("Test Failed");}
  free(src);
  free(tmp);

  return 0;
}

void merge_sort(double *src, double *tmp, int size) {
  int i, stride=100;
    for(i=0;i+stride<size;i=i+stride){
	    ins_sort(src, i, i+stride-1);
    }
    if(i<size){ ins_sort(src, i, size-1); }

   while(stride<size){	
    for(i=0;i+2*stride<size;i=i+2*stride){
	    merge_r(src, tmp, i, i+stride, i+2*stride-1);
    }
    if(i+stride<size){ merge_r(src,tmp, i, i+stride, size-1); } 
    stride=2*stride;
   }
}
void ins_sort(double *src, int start, int end){
	for(int pos=start+1; pos<=end; pos++){
		int temp=src[pos];
		while(pos>start && src[pos-1]>temp){ src[pos]=src[pos-1]; pos=pos-1; }
		if(pos>=start){ src[pos]=temp; }
	}	
}

void merge_r(double *src, double *tmp, int start, int mid, int end) {
int i=start, j=mid, k=start;
	while (i <=mid-1 && j <=end) {
		if (src[i] <= src[j]) { tmp[k++] = src[i++]; }
	       	else { tmp[k++] = src[j++]; }
	}
	while (i<=mid-1) { tmp[k++] = src[i++]; }	
	while (j<=end) { tmp[k++] = src[j++];}
	i=start;
	j=end;
	while(i<=j){ src[i]=tmp[i]; i++;}
}
