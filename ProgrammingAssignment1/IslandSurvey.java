// File name: IslandSurvey.java
// Island Survey program that reads input from file and counts connected components (islands)
// Name: Rayyan Lodhi
// Student #: 300437765

import java.util.*;

public class IslandSurvey {
    
    // Helper class to store island analysis results
    static class IslandResult {
        int islandCount;
        List<Integer> islandSizes;
        int totalArea;
        
        IslandResult(int count, List<Integer> sizes, int area) {
            this.islandCount = count;
            this.islandSizes = sizes;
            this.totalArea = area;
        }
    }
    

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Read the dimensions of the map
            int rows = scanner.nextInt();
            int cols = scanner.nextInt();
            
            // Create the map grid
            char[][] map = new char[rows][cols];
            
            // Read the map data
            for (int i = 0; i < rows; i++) {
                String line = scanner.next();
                for (int j = 0; j < cols; j++) {
                    map[i][j] = line.charAt(j);
                }
            }
            
            // Read the final 0 (if present)
            if (scanner.hasNextInt()) {
                int endMarker = scanner.nextInt();
            }
            
            // Process the map and find islands
            IslandResult result = analyzeIslands(map, rows, cols);
            
            // Output the results
            System.out.println(result.islandCount);
            
            // Output island sizes in decreasing order
            if (result.islandSizes.isEmpty()) {
                System.out.println(-1);
            } else {
                for (int i = 0; i < result.islandSizes.size(); i++) {
                    if (i > 0) System.out.print(" ");
                    System.out.print(result.islandSizes.get(i));
                }
                System.out.println();
            }
            
            // Output total area
            System.out.println(result.totalArea);
            
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Analyzes islands in the map using Union-Find data structure
     * Follows the exact algorithm specification from the assignment
     * Returns island count, sizes in decreasing order, and total area
     */
    private static IslandResult analyzeIslands(char[][] map, int rows, int cols) {
        // Create S by T auxiliary array cluster to keep track of cluster positions
        // with all entries initialized to null
        Node<Integer>[][] cluster = new Node[rows][cols];
        
        // Create BP a new object of the class Partition
        Partition<Integer> BP = new Partition<>();
        
        // for each black grid point i,j:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // p = BP.makeCluster(info(i,j));
                    // cluster[i,j] = p
                    int positionId = i * cols + j; // info(i,j) - unique identifier
                    cluster[i][j] = BP.makeCluster(positionId);
                }
            }
        }
        
        // for each black grid point i,j:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // for each black grid point k,l adjacent to i,j:
                    int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}}; // right, down, left, up
                    for (int[] dir : directions) {
                        int k = i + dir[0];
                        int l = j + dir[1];
                        
                        // Check if (k,l) is within bounds and is black
                        if (k >= 0 && k < rows && l >= 0 && l < cols && map[k][l] == '1') {
                            // if BP.find(cluster[i,j]) != BP.find(cluster[k,l]) then
                            if (BP.find(cluster[i][j]) != BP.find(cluster[k][l])) {
                                // BP.union(cluster[i,j], cluster[k,l])
                                BP.union(cluster[i][j], cluster[k][l]);
                            }
                        }
                    }
                }
            }
        }
        
        // Get island count and sizes
        int islandCount = BP.numberOfClusters();
        List<Integer> islandSizes = BP.clusterSizes(); // Already in decreasing order
        int totalArea = islandSizes.stream().mapToInt(Integer::intValue).sum();
        
        return new IslandResult(islandCount, islandSizes, totalArea);
    }
}
