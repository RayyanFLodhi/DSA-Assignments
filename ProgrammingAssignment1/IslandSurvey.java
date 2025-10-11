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
    
    // Simple class to store position info(i,j) as required by assignment
    static class PositionInfo {
        int row, col;
        PositionInfo(int row, int col) {
            this.row = row;
            this.col = col;
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
            
            // Read the initial map data
            for (int i = 0; i < rows; i++) {
                String line = scanner.next();
                for (int j = 0; j < cols; j++) {
                    map[i][j] = line.charAt(j);
                }
            }
            
            // Read the number of phases F
            int numPhases = scanner.nextInt();
            
            // Initialize the partition system for phases
            Partition<PositionInfo> BP = new Partition<>();
            Node<PositionInfo>[][] cluster = new Node[rows][cols];
            
            // Phase 0: Initial survey
            IslandResult result = processInitialPhase(map, rows, cols, BP, cluster);
            printResults(result);
            
            // Add empty line after initial phase if there are more phases
            if (numPhases > 0) {
                System.out.println();
            }
            
            // Process subsequent phases
            for (int phase = 0; phase < numPhases; phase++) {
                // Read L (number of new land squares for this phase)
                int L = scanner.nextInt();
                
                // Read L pairs of coordinates (2L numbers total)
                List<PositionInfo> newPositions = new ArrayList<>();
                for (int k = 0; k < L; k++) {
                    int i = scanner.nextInt();
                    int j = scanner.nextInt();
                    newPositions.add(new PositionInfo(i, j));
                }
                
                // Process the new phase
                result = processNewPhase(map, rows, cols, BP, cluster, newPositions);
                printResults(result);
                
                // Add empty line between phases (except after the last phase)
                if (phase < numPhases - 1) {
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    private static void printResults(IslandResult result) {
        // Output the results
        System.out.println(result.islandCount);
        
        // Output island sizes in decreasing order (each on separate line)
        if (result.islandSizes.isEmpty()) {
            System.out.println(-1);
        } else {
            for (int size : result.islandSizes) {
                System.out.println(size);
            }
        }
        
        // Output total area
        System.out.println(result.totalArea);
    }
    
    /**
     * Processes the initial phase (Phase 0) - creates initial partition from the map
     * Follows the exact algorithm specification from the assignment
     */
    private static IslandResult processInitialPhase(char[][] map, int rows, int cols, 
                                                   Partition<PositionInfo> BP, Node<PositionInfo>[][] cluster) {
        // Create S by T auxiliary array cluster to keep track of cluster positions
        // with all entries initialized to null (already done in main)
        
        // for each black grid point i,j:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // p = BP.makeCluster(info(i,j));
                    // cluster[i,j] = p
                    PositionInfo position = new PositionInfo(i, j); // info(i,j) - stores i and j coordinates
                    cluster[i][j] = BP.makeCluster(position);
                }
            }
        }
        
        // for each black grid point i,j:
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // OPTIMIZATION: Check only right and down directions to avoid redundant checks
                    // This halves the number of adjacency checks while maintaining correctness
                    
                    // Check right neighbor (i, j+1)
                    if (j + 1 < cols && map[i][j + 1] == '1') {
                        if (BP.find(cluster[i][j]) != BP.find(cluster[i][j + 1])) {
                            BP.union(cluster[i][j], cluster[i][j + 1]);
                        }
                    }
                    
                    // Check down neighbor (i+1, j)
                    if (i + 1 < rows && map[i + 1][j] == '1') {
                        if (BP.find(cluster[i][j]) != BP.find(cluster[i + 1][j])) {
                            BP.union(cluster[i][j], cluster[i + 1][j]);
                        }
                    }
                }
            }
        }
        
        return getCurrentResults(BP);
    }
    
    /**
     * Processes a new phase by adding new black positions and updating islands
     * Follows the exact algorithm specification from the assignment
     */
    private static IslandResult processNewPhase(char[][] map, int rows, int cols,
                                               Partition<PositionInfo> BP, Node<PositionInfo>[][] cluster,
                                               List<PositionInfo> newPositions) {
        
        // for each point i,j in the new list
        for (PositionInfo pos : newPositions) {
            int i = pos.row;
            int j = pos.col;
            
            // p = BP.makeCluster(info(i,j));
            // cluster[i,j] = p
            cluster[i][j] = BP.makeCluster(pos);
            
            // change grid point i,j to black
            map[i][j] = '1';
        }
        
        // for each point i,j in the new list
        for (PositionInfo pos : newPositions) {
            int i = pos.row;
            int j = pos.col;
            
            // for each black grid point k,l adjacent to i,j:
            // Check all 4 directions for new positions (they might connect to existing islands)
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
        
        return getCurrentResults(BP);
    }
    
    /**
     * Gets the current island analysis results from the partition
     */
    private static IslandResult getCurrentResults(Partition<PositionInfo> BP) {
        // Get island count and sizes
        int islandCount = BP.numberOfClusters();
        List<Integer> islandSizes = BP.clusterSizes(); // Already in decreasing order
        int totalArea = islandSizes.stream().mapToInt(Integer::intValue).sum();
        
        return new IslandResult(islandCount, islandSizes, totalArea);
    }
}
