// File name: IslandSurvey.java
// Island Survey program that reads input from file and counts connected components (islands)
// Name: Rayyan Lodhi
// Student #: 300437765

import java.io.*;
import java.util.*;

public class IslandSurvey {
    
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
            int islandCount = countIslands(map, rows, cols);
            
            // Output the result
            System.out.println(islandCount);
            
        } catch (Exception e) {
            System.err.println("Error reading input: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Counts the number of islands (connected components of '1's) in the map
     * using Union-Find data structure
     */
    private static int countIslands(char[][] map, int rows, int cols) {
        Partition<Integer> partition = new Partition<>();
        Node<Integer>[][] nodes = new Node[rows][cols];
        
        // Create nodes for all land cells (1s)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // Create a unique identifier for this position
                    int positionId = i * cols + j;
                    nodes[i][j] = partition.makeCluster(positionId);
                }
            }
        }
        
        // Union adjacent land cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    // Check right neighbor
                    if (j + 1 < cols && map[i][j + 1] == '1') {
                        partition.union(nodes[i][j], nodes[i][j + 1]);
                    }
                    // Check bottom neighbor
                    if (i + 1 < rows && map[i + 1][j] == '1') {
                        partition.union(nodes[i][j], nodes[i + 1][j]);
                    }
                }
            }
        }
        
        return partition.numberOfClusters();
    }
    
    /**
     * Alternative method using DFS (Depth-First Search) approach
     * This is commented out but available as an alternative implementation
     */
    /*
    private static int countIslandsDFS(char[][] map, int rows, int cols) {
        boolean[][] visited = new boolean[rows][cols];
        int islandCount = 0;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1' && !visited[i][j]) {
                    dfs(map, visited, i, j, rows, cols);
                    islandCount++;
                }
            }
        }
        
        return islandCount;
    }
    
    private static void dfs(char[][] map, boolean[][] visited, int row, int col, int rows, int cols) {
        if (row < 0 || row >= rows || col < 0 || col >= cols || 
            map[row][col] == '0' || visited[row][col]) {
            return;
        }
        
        visited[row][col] = true;
        
        // Visit all 4 directions
        dfs(map, visited, row + 1, col, rows, cols); // down
        dfs(map, visited, row - 1, col, rows, cols); // up
        dfs(map, visited, row, col + 1, rows, cols); // right
        dfs(map, visited, row, col - 1, rows, cols); // left
    }
    */
}
