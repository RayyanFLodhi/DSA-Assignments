// Node.java
// Position node for the sequence-based Partition ADT (singly linked)

public class Node<E> {
    E elem;                          // stored element
    Node<E> next;                    // next in the cluster's list
    Partition.Cluster<E> cluster;    // back-pointer to owning cluster (sequence)

    public Node(E elem) {
        this.elem = elem;
    }
}
