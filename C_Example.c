#include <stdio.h>
#include <stdlib.h>

void display_repeats(int *a, int size){
    int i;
    int *check = malloc(size * sizeof check[0]);

    for(i = 0; i < size; i++){
        check[i] = 0;
    }

    for(i = 0; i < size; i++){
        check[a[i]] += 1;
    }

    for(i = 0; i < size; i++){
        if(check[i] > 1){
            printf("%d repeats %d times\n", i, check[i]);

                }
    }
    free(check);
}

int main(void){
    int array_size;
    int *my_array;
    int i = 0;

    printf("Enter the size of the array: ");
    scanf("%d", &array_size);

    my_array = malloc(array_size *sizeof my_array[0]);
    if(NULL == my_array){
        fprintf(stderr, "Memory allocation failed!\n");
        return EXIT_FAILURE;
    }
