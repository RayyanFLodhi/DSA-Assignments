public interface Partition<E> {
    void makeCluster(E x);
    void union(E p, E q);
    E find(E p);
}
