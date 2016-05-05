// psumemory.h
//

#include <sys/mman.h>
#include <time.h>

#ifdef __x86_64__
typedef unsigned long address_t;
#else
typedef unsigned int address_t;
#endif


// header
typedef struct Header
{
	int size;
	struct Header *next;
} Header;
/*
// free list node
typedef struct node_t
{
	int size;
	struct node_t *next;
	int free;
} node_t;
*/
int psumeminit(int algo, int sizeOfRegion);
void *psumalloc(int size);
int psufree(void *ptr);
void psumemdump();

