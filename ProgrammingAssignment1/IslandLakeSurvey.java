// File name: IslandLakeSurvey.java
// Island Lake Survey program that extends Part 2A to identify and count lakes within islands
// Name: Rayyan Lodhi
// Student #: 300437765

import java.util.*;

public class IslandLakeSurvey {

    /* ------------------------- Helper Data Types ------------------------- */

    // Holds the printed results for a survey block
    static class IslandLakeResult {
        int islandCount;
        List<Integer> islandSizes;   // sorted decreasing; includes lake areas
        int totalIslandArea;         // sum of islandSizes
        int totalLakeCount;
        int totalLakeArea;

        IslandLakeResult(int islandCount, List<Integer> islandSizes, int totalIslandArea,
                         int totalLakeCount, int totalLakeArea) {
            this.islandCount = islandCount;
            this.islandSizes = islandSizes;
            this.totalIslandArea = totalIslandArea;
            this.totalLakeCount = totalLakeCount;
            this.totalLakeArea = totalLakeArea;
        }
    }

    // Position info to store (i, j) inside Partition nodes
    static class PositionInfo {
        int row, col;
        PositionInfo(int row, int col) { this.row = row; this.col = col; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PositionInfo that = (PositionInfo) o;
            return row == that.row && col == that.col;
        }
        @Override public int hashCode() { return Objects.hash(row, col); }
    }

    // Lake data (area + which island leader contains it)
    static class LakeInfo {
        int area;
        Node<PositionInfo> containingIsland;
        LakeInfo(int area, Node<PositionInfo> containingIsland) {
            this.area = area;
            this.containingIsland = containingIsland;
        }
    }

    /* ------------------------------- Main ------------------------------- */

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            int rows = scanner.nextInt();
            int cols = scanner.nextInt();

            char[][] map = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                String line = scanner.next();
                for (int j = 0; j < cols; j++) map[i][j] = line.charAt(j);
            }

            int numPhases = scanner.nextInt();

            // Partitions and cluster grids
            Partition<PositionInfo> BP = new Partition<>();             // black points (islands)
            Partition<PositionInfo> WP = new Partition<>();             // white points (potential lakes)
            @SuppressWarnings("unchecked")
            Node<PositionInfo>[][] cluster = new Node[rows][cols];      // black cluster positions
            @SuppressWarnings("unchecked")
            Node<PositionInfo>[][] whiteCluster = new Node[rows][cols]; // white cluster positions

            // ----- Phase 0: build BP and WP from the initial map -----
            IslandLakeResult result = processInitialPhase(map, rows, cols, BP, WP, cluster, whiteCluster);
            printResults(result);

            if (numPhases > 0) System.out.println();

            // ----- Subsequent phases -----
            for (int phase = 0; phase < numPhases; phase++) {
                int L = scanner.nextInt();
                List<PositionInfo> newPositions = new ArrayList<>();
                for (int k = 0; k < L; k++) {
                    int i = scanner.nextInt();
                    int j = scanner.nextInt();
                    newPositions.add(new PositionInfo(i, j));
                }
            
                result = processNewPhase(map, rows, cols, BP, cluster, newPositions);
                printResults(result);
            
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

    /* ------------------------- Printing (2B format) ------------------------- */

    private static void printResults(IslandLakeResult result) {
        // 1) number of islands
        System.out.println(result.islandCount);

        // 2) list of island sizes (decreasing) or -1 if none
        if (result.islandSizes.isEmpty()) {
            System.out.println(-1);
        } else {
            for (int sz : result.islandSizes) System.out.println(sz);
        }

        // 3) total island area
        System.out.println(result.totalIslandArea);

        // 4–5) lakes lines: suppress when there are no islands AND no lakes (matches sample)
        if (!(result.islandCount == 0 && result.totalLakeCount == 0 && result.totalLakeArea == 0)) {
            System.out.println(result.totalLakeCount);
            System.out.println(result.totalLakeArea);
        }
    }

    /* ----------------------------- Algorithms ----------------------------- */

    // Phase 0: build BP with 4-neighbor connectivity; build WP with 8-neighbor connectivity
    private static IslandLakeResult processInitialPhase(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        // Create black clusters
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    PositionInfo pos = new PositionInfo(i, j);
                    cluster[i][j] = BP.makeCluster(pos);
                }
            }
        }
        // Union adjacent black cells (4-neighbor)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    if (j + 1 < cols && map[i][j + 1] == '1'
                            && BP.find(cluster[i][j]) != BP.find(cluster[i][j + 1])) {
                        BP.union(cluster[i][j], cluster[i][j + 1]);
                    }
                    if (i + 1 < rows && map[i + 1][j] == '1'
                            && BP.find(cluster[i][j]) != BP.find(cluster[i + 1][j])) {
                        BP.union(cluster[i][j], cluster[i + 1][j]);
                    }
                }
            }
        }

        // Create white clusters (8-neighbor / corners allowed)
        buildWhitePartitionFromScratch(map, rows, cols, WP, whiteCluster);

        return getCurrentResults(map, rows, cols, BP, WP, cluster, whiteCluster);
    }

    // New phase: add new black squares, union with adjacent blacks, rebuild WP, then compute results
    private static IslandLakeResult processNewPhase(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Node<PositionInfo>[][] cluster,
            List<PositionInfo> newPositions) {

        // Create new singleton clusters and flip to black
        for (PositionInfo pos : newPositions) {
            int i = pos.row, j = pos.col;
            cluster[i][j] = BP.makeCluster(pos);
            map[i][j] = '1';
        }

        // Union with adjacent existing blacks (4-neighbor)
        int[][] sideDirs = {{0,1},{1,0},{0,-1},{-1,0}};
        for (PositionInfo pos : newPositions) {
            int i = pos.row, j = pos.col;
            for (int[] d : sideDirs) {
                int ni = i + d[0], nj = j + d[1];
                if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && map[ni][nj] == '1') {
                    if (BP.find(cluster[i][j]) != BP.find(cluster[ni][nj])) {
                        BP.union(cluster[i][j], cluster[ni][nj]);
                    }
                }
            }
        }

        // Rebuild the white partition from scratch (simplest & correct)
        Partition<PositionInfo> WP = new Partition<>();
        @SuppressWarnings("unchecked")
        Node<PositionInfo>[][] whiteCluster = new Node[rows][cols];
        buildWhitePartitionFromScratch(map, rows, cols, WP, whiteCluster);

        return getCurrentResults(map, rows, cols, BP, WP, cluster, whiteCluster);
    }

    // Build WP with 8-neighbor connectivity (corner connectivity)
    private static void buildWhitePartitionFromScratch(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> WP, Node<PositionInfo>[][] whiteCluster) {

        // Make clusters
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '0') {
                    PositionInfo pos = new PositionInfo(i, j);
                    whiteCluster[i][j] = WP.makeCluster(pos);
                }
            }
        }
        // Union 8-neighbor whites
        int[][] allDirs = {{0,1},{1,0},{0,-1},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '0') {
                    for (int[] d : allDirs) {
                        int ni = i + d[0], nj = j + d[1];
                        if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && map[ni][nj] == '0') {
                            if (WP.find(whiteCluster[i][j]) != WP.find(whiteCluster[ni][nj])) {
                                WP.union(whiteCluster[i][j], whiteCluster[ni][nj]);
                            }
                        }
                    }
                }
            }
        }
    }

    /* -------------------------- Result Computation -------------------------- */

    private static IslandLakeResult getCurrentResults(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        // 1) Identify lakes from white components
        List<LakeInfo> lakes = identifyLakes(map, rows, cols, BP, WP, cluster, whiteCluster);
        int totalLakeCount = lakes.size();
        int totalLakeArea  = lakes.stream().mapToInt(l -> l.area).sum();

        // 2) Build base island sizes by BP leader (count black cells per island)
        Map<Node<PositionInfo>, Integer> baseSizes = new HashMap<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    Node<PositionInfo> leader = BP.find(cluster[i][j]);
                    baseSizes.merge(leader, 1, Integer::sum);
                }
            }
        }

        // 3) Add lake areas to their containing island (by the same leader)
        Map<Node<PositionInfo>, Integer> lakeByIsland = new HashMap<>();
        for (LakeInfo lake : lakes) {
            lakeByIsland.merge(lake.containingIsland, lake.area, Integer::sum);
        }

        List<Integer> finalSizes = new ArrayList<>();
        for (Map.Entry<Node<PositionInfo>, Integer> e : baseSizes.entrySet()) {
            int withLakes = e.getValue() + lakeByIsland.getOrDefault(e.getKey(), 0);
            finalSizes.add(withLakes);
        }
        finalSizes.sort(Comparator.reverseOrder());

        int islandCount = baseSizes.size();
        int totalIslandArea = finalSizes.stream().mapToInt(Integer::intValue).sum();

        return new IslandLakeResult(islandCount, finalSizes, totalIslandArea, totalLakeCount, totalLakeArea);
    }

    // Identify lakes: WP clusters that (a) do not touch the map edge and (b) by SIDES touch exactly one island
    private static List<LakeInfo> identifyLakes(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        List<LakeInfo> lakes = new ArrayList<>();
        Set<Node<PositionInfo>> seen = new HashSet<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '0' && whiteCluster[i][j] != null) {
                    Node<PositionInfo> head = WP.find(whiteCluster[i][j]);
                    if (seen.add(head)) {
                        LakeInfo lake = checkIfLake(map, rows, cols, head, BP, WP, cluster);
                        if (lake != null) lakes.add(lake);
                    }
                }
            }
        }
        return lakes;
    }

    // A white WP cluster is a lake if it: (1) does not touch the outside edge, and (2) by SIDES touches exactly one island
    private static LakeInfo checkIfLake(
            char[][] map, int rows, int cols, Node<PositionInfo> whiteHead,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP, Node<PositionInfo>[][] cluster) {

        List<Node<PositionInfo>> whites = WP.clusterPositions(whiteHead);
        int area = whites.size();

        boolean touchesEdge = false;
        Set<Node<PositionInfo>> adjacentIslands = new HashSet<>();

        int[][] sideDirs = {{0,1},{1,0},{0,-1},{-1,0}}; // SIDES ONLY for adjacency check

        for (Node<PositionInfo> wn : whites) {
            PositionInfo p = WP.element(wn);
            int i = p.row, j = p.col;

            // touches outside edge?
            if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1) {
                touchesEdge = true;
                break;
            }

            // collect adjacent island leaders by sides
            for (int[] d : sideDirs) {
                int ni = i + d[0], nj = j + d[1];
                if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && map[ni][nj] == '1') {
                    adjacentIslands.add(BP.find(cluster[ni][nj]));
                }
            }
        }

        if (!touchesEdge && adjacentIslands.size() == 1) {
            Node<PositionInfo> containingIsland = adjacentIslands.iterator().next();
            return new LakeInfo(area, containingIsland);
        }
        return null;
    }
}
