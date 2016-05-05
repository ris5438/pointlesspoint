
// @author Rishabh Sawhney
// Implementing own versons of malloc and free

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/mman.h>
#include "psumemory.h"
#include <assert.h>

#ifdef __x86_64__
typedef unsigned long address_t;
#else
typedef unsigned int address_t;
#endif

// pointer of the free list
static Header *head;
int ALGO;	// which algorithm to use




void printfreelist (void)
{
    Header *node = head;
    printf("\n\nHead-> ");
    while (node!=NULL)
    {
        printf("|%p:%d:%p|-> ", node, node->size, node->next);
        node = node->next;
    }
    printf(" -> End\n\n");
}


// return value : 0 success, -1 otherwise
int psumeminit(int algo, int sizeOfRegion)
{
	// const int pagesize = getpagesize(); 
	assert (algo == 0 || algo == 1);
	ALGO = algo;
	
	// call to mmap
	head = mmap(NULL, sizeOfRegion, 
			PROT_READ|PROT_WRITE, MAP_ANON|MAP_PRIVATE, -1, 0);
	head->size = sizeOfRegion - sizeof(Header);
	head->next = NULL;
	
	if (head == NULL)
		return -1;
	else
		return 0;

}

void *psumalloc(int size)
{
	// check for exceptions
	if (size <= 0)
		return NULL;

	if (!head)
		return NULL;

	///////////////////////////////////////////////////////////////////////////////////////////
	
	int nunits;
	nunits = (size + sizeof(Header)); //- 1)/sizeof(Header) + 1;
	assert (ALGO == 0 || ALGO == 1);
	Header *prev  = NULL;
	Header *current = head;
	Header *before = NULL;	// Before the best_fit



	if (ALGO == 0)		// Best Fit
	{
		// Best fit
		// 1. Search through the free list and find chunks of free memory that are as big 
		//    or bigger than the requested size.
		// 2. Return the one that is the smallest in that group
		//		Header *current = head;
        Header *best_fit = head;
	
		for (current = head; current != NULL; prev = current, current = current->next)
		{
			// if current node is a better fit
			if ((current->size >= size) && (current->size <= best_fit->size))
			{
				best_fit = current;
				before = prev;
			}
		}


        if (best_fit == NULL)
            return NULL;
        if (best_fit->size < size)
        {
            return NULL;
        }
        

		// check if best fit is perfect fit
		if (best_fit->size == size)
		{
			// remove from free list
			before->next = best_fit->next;
		}
		else
		{
			// Two big, split in two
			best_fit->size -= nunits;
            best_fit += ((best_fit->size/sizeof(Header)) + 1);
			best_fit->size = size;
		}

		return (void *)(best_fit + 1);
	}
	else if (ALGO == 1)		// Worst Fit
	{
		// worst fit
		// 1. Find the largest chunk and return the requested amount
		// 2. Keep the remaining on the free list
		// Header *current = head;
		Header *worst_fit = head;
        
        // check the free list
  //      printfreelist();

		for (current = head; current != NULL; prev = current, current = current->next)
		{
			// if current node is worse fit
			if ((current->size >= size) && (current->size >= worst_fit->size))
			{
				worst_fit = current;
				before = prev;
			}
		}
        
        
        if (worst_fit == NULL)
            return NULL;
        if (worst_fit->size < size)
        {
            return NULL;
        }
		// check if best fit is perfect fit
		if (worst_fit->size == size)
		{
			// remove from free list
			before->next = worst_fit->next;
		}
		else
		{
			worst_fit->size -= nunits;
            worst_fit += ((worst_fit->size/sizeof(Header)) + 1);
			worst_fit->size = size;
		}
		return (void *) (worst_fit + 1);
	}

	return NULL;
	
}

int psufree(void *ptr)
{
	Header *hptr = (Header *)ptr - 1;
	// If ptr is NULL
	if(!ptr)
		return -1;

    
	Header *current  = head;
	while ((current->next != NULL) && !((hptr > current) && (hptr <= current->next)))
    {
        // check if hptr is already allotted
        if (hptr == current->next)
        {
            return -1;
        }
        current = current->next;
    }
    
    // if its to be placed at the end
    if (current->next == NULL)
    {
        current->next = hptr;
        hptr->next = NULL;
    }
    
    // check if hptr is already allotted
    if (hptr == current->next)
    {
        return -1;
    }

    // coalesce forward
    if (hptr + ((hptr->size + sizeof(Header))/sizeof(Header)) == current->next)
    {
        hptr->size += ((current->next->size)/sizeof(Header));  // lengthen
        hptr->next = current->next->next;   // copy pointer
    }
    else
    {
        hptr->next = current->next;
        current->next = hptr;
    }
    // coalesce behind
    if (current + ((current->size + sizeof(Header))/sizeof(Header)) == hptr)
    {
        current->size += hptr->size;    // lengthen
        current->next = hptr->next;
    }
	
	return 0;

}

void psumemdump()
{
    printfreelist();
}
