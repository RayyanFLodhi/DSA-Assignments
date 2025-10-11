// File name: Partition.java
// Sequence (singly linked list) implementation of the Partition ADT utilizing Clusters and Nodes.
// Name: Rayyan Lodhi
// Student #: 300437765

import java.util.*;

public class Partition<E> {

    // A Cluster "owns" one singly linked list (the sequence).
    public static class Cluster<E> {
        Node<E> head, tail;  // leader == head
        int size;

        Cluster(Node<E> singleton) {
            head = tail = singleton;
            size = 1;
        }
    }

    // Tracks all Clusters with an ArrayList
    private List<Cluster<E>> clusters = new ArrayList<>();

    // Create singleton cluster, return its position.
    public Node<E> makeCluster(E x) {
        Node<E> newNode = new Node<>(x);
        Cluster<E> newCluster = new Cluster<>(newNode);
        newNode.cluster = newCluster;
        clusters.add(newCluster);

        return newNode;
    }

    // Merges clusters containing p and q (move smaller into larger).
    public void union(Node<E> p, Node<E> q) {
        Cluster<E> clusterP = p.cluster;
        Cluster<E> clusterQ = q.cluster;

        // edge case (if both clusters are same)
        if (clusterP == clusterQ) {
            return;
        }

        Cluster<E> largerCluster = null;
        Cluster<E> smallerCluster = null;

        // Finding min/max of nodes
        if (clusterP.size >= clusterQ.size) {
            largerCluster = clusterP;
            smallerCluster = clusterQ;
        }
        else {
            largerCluster = clusterQ;
            smallerCluster = clusterP;
        }

        // reassigning pointers
        int smallerSize = smallerCluster.size;
        Node<E> smallerHeadNode = smallerCluster.head;
        Node<E> smallerTailNode = smallerCluster.tail;
        largerCluster.tail.next = smallerCluster.head;
        largerCluster.tail = smallerTailNode;
        largerCluster.size += smallerSize;

        // looping over smaller linked list to reassign cluster node
        Node<E> currentNode = smallerHeadNode;

        while (currentNode != null) {
            currentNode.cluster = largerCluster;
            currentNode = currentNode.next;
        }

        // removing smaller cluster
        clusters.remove(smallerCluster);
    }

    // Returns the Cluster of the given Node.
    public Node<E> find(Node<E> p) {
        return p.cluster.head;
    }

    // Returns given element at Position P.
    public E element(Node<E> p) { 
        return p.elem; 
    }

    // Returns number of clusters in ArrayList (clusters).
    public int numberOfClusters() { 
        return clusters.size(); 
    }

    // Returning Cluster Size
    public int clusterSize(Node<E> p) { 
        return p.cluster.size; 
    }

    // list the positions in p's cluster, in sequence order.
    public List<Node<E>> clusterPositions(Node<E> p) {
        ArrayList<Node<E>> outputList = new ArrayList<>(p.cluster.size);
        Node<E> currentNode = p.cluster.head;

        while (currentNode != null) {
            outputList.add(currentNode);
           
            if (currentNode == p.cluster.tail) {
                break;
            }
            currentNode = currentNode.next;
        }

        return outputList;
    }

    // Returns list of integers determining the size of each cluster in decreasing order
    public List<Integer> clusterSizes() {
        ArrayList<Integer> sizes = new ArrayList<>(clusters.size());
        
        // loops overs clusters array list, appending the size of each cluster to the new array list.
        for (Cluster<E> currentCluster : clusters) {
            sizes.add(currentCluster.size);
        }

        // sorts and returns in decreasing order
        sizes.sort(Comparator.reverseOrder()); 
        return sizes;
    }
}
