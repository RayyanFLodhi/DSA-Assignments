public class ForgetfulStack<E> {
    private E[] stack;
    private int top;       // next free slot (top pointer)
    private int bottom;    // index of the oldest element
    private int size;      // number of elements in stack
    private int capacity;

    @SuppressWarnings("unchecked")
    public ForgetfulStack(int capacity) {
        this.capacity = capacity;
        this.stack = (E[]) new Object[capacity];
        this.top = 0;
        this.bottom = 0;
        this.size = 0;
    }

    // Support methods
    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public E top() {
        if (isEmpty()) return null;
        int idx = (top - 1 + capacity) % capacity; // element just before top
        return stack[idx];
    }

    // Main methods
    public void push(E obj) {
        if (size == capacity) {
            throw new IllegalStateException("Stack full");
        }
        stack[top] = obj;
        top = (top + 1) % capacity; // move forward circularly
        size++;
    }

    public E pop() {
        if (isEmpty()) return null;
        top = (top - 1 + capacity) % capacity; // step back circularly
        E poppedItem = stack[top];
        stack[top] = null; // optional: clear for GC
        size--;
        if (size == 0) bottom = top; // reset when empty
        return poppedItem;
    }

    public void forget(int k) {
        if (k <= 0 || isEmpty()) return;
        if (k >= size) {
            size = 0;
            bottom = top;
            return;
        }
        bottom = (bottom + k) % capacity;
        size -= k;
    }
}
