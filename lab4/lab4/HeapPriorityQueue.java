/**
 * Array Heap implementation of a priority queue
 * @author Lachlan Plant
 */
public class HeapPriorityQueue<K extends Comparable,V> implements PriorityQueue<K ,V> {
    
    private Entry[] storage; //The Heap itself in array form
    private int tail;        //Index of last element in the heap
    
    /**
    * Default constructor
    */
    public HeapPriorityQueue(){
        this(25);
    }
    
    /**
    * HeapPriorityQueue constructor with max storage of size elements
    */
    public HeapPriorityQueue(int size){
        storage = new Entry[size];
        tail = -1;
    }
    
    /****************************************************
     * 
     *             Priority Queue Methods
     * 
     ****************************************************/
    
    /**
    * Returns the number of items in the priority queue.
    * O(1)
    * @return number of items
    */
    public int size(){
        return tail + 1;
    }

    /**
    * Tests whether the priority queue is empty.
    * O(1)
    * @return true if the priority queue is empty, false otherwise
    */
    public boolean isEmpty(){
        if (size() == 0) {
            return true;
        }
        return false;
    }
    
    /**
    * Inserts a key-value pair and returns the entry created.
    * O(log(n))
    * @param key     the key of the new entry
    * @param value   the associated value of the new entry
    * @return the entry storing the new key-value pair
    * @throws IllegalArgumentException if the heap is full
    */
    public Entry<K,V> insert(K key, V value) throws IllegalArgumentException{
        if (tail + 1 == storage.length) {
            throw new IllegalArgumentException("Heap is full");
        }
        Entry<K, V> newEntry = new Entry<>(key, value);
        storage[tail + 1] = newEntry;
        tail += 1;
        upHeap(tail);
        return newEntry;
    }
    
    /**
    * Returns (but does not remove) an entry with minimal key.
    * O(1)
    * @return entry having a minimal key (or null if empty)
    */
    public Entry<K,V> min(){
        if (isEmpty()) {
            return null;
        }

        return storage[0];
    } 
    
    /**
    * Removes and returns an entry with minimal key.
    * O(log(n))
    * @return the removed entry (or null if empty)
    */ 
    public Entry<K,V> removeMin(){
        if (isEmpty()) {
            return null;
        }

        Entry<K, V> min = storage[0];
        storage[0] = storage[tail];
        storage[tail] = null;
        tail --;

        if (!isEmpty()) {
            downHeap(0);
        }

        return min;
    }  
    
    
    /****************************************************
     * 
     *           Methods for Heap Operations
     * 
     ****************************************************/
    
    /**
    * Algorithm to place element after insertion at the tail.
    * O(log(n))
    */
    private void upHeap(int location) {
        while (location > 0) {
            int parentIndex = (location - 1) / 2;

            // Compare using raw Comparable
            if (((Comparable) storage[location].getKey())
                    .compareTo(storage[parentIndex].getKey()) >= 0) {
                break; // heap property satisfied
            }

            swap(location, parentIndex);
            location = parentIndex;
        }
    }


    
    /**
    * Algorithm to place element after removal of root and tail element placed at root.
    * O(log(n))
    */
    private void downHeap(int location) {
        while (true) {
            int leftIndex = 2 * location + 1;
            int rightIndex = 2 * location + 2;
            int smallest = location;

            // Compare left child
            if (leftIndex <= tail &&
                    ((Comparable) storage[leftIndex].getKey())
                            .compareTo(storage[smallest].getKey()) < 0) {
                smallest = leftIndex;
            }

            // Compare right child
            if (rightIndex <= tail &&
                    ((Comparable) storage[rightIndex].getKey())
                            .compareTo(storage[smallest].getKey()) < 0) {
                smallest = rightIndex;
            }

            if (smallest == location) break;

            swap(location, smallest);
            location = smallest;
        }
    }


    
    /**
    * Find parent of a given location,
    * Parent of the root is the root
    * O(1)
    */
    private int parent(int location){
        return (location-1)/2;
    }
    
   
    /**
    * Inplace swap of 2 elements, assumes locations are in array
    * O(1)
    */
    private void swap(int location1, int location2){
        Entry<K,V> element1 = storage[location1];
        storage[location1] = storage[location2];
        storage[location2] = element1;
    }
    
}
