// File name: IslandLakeSurvey.java
// Island Lake Survey program that extends Part 2A to identify and count lakes within islands
// Name: Rayyan Lodhi
// Student #: 300437765

import java.util.*;

public class IslandLakeSurvey {

    // Holds the printed results for a survey block
    static class IslandLakeResult {
        int islandCount;
        List<Integer> islandSizes;   // sorted decreasing; includes lake areas
        int totalIslandArea;         // sum of islandSizes
        int totalLakeCount;
        int totalLakeArea;

        // Group all output pieces together so printing code stays simple and consistent
        IslandLakeResult(int islandCount, List<Integer> islandSizes, int totalIslandArea, int totalLakeCount, int totalLakeArea) {
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

        // Store the grid coordinates as-is; Partition uses these as the element payload
        PositionInfo(int row, int col) {
            this.row = row; this.col = col;
        }

        // Equality is based purely on coordinates. This lets us use PositionInfo safely in sets/maps.
        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PositionInfo that = (PositionInfo) o;
            return row == that.row && col == that.col;
        }

        @Override public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    // Captures a lake's size and which island leader owns it
    static class LakeInfo {
        int area;
        Node<PositionInfo> containingIsland;
        LakeInfo(int area, Node<PositionInfo> containingIsland) {
            this.area = area;
            this.containingIsland = containingIsland;
        }
    }

    // Main function running all methods together
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            int rows = scanner.nextInt();
            int cols = scanner.nextInt();

            char[][] map = new char[rows][cols];

            // Read S lines of 0/1 characters. We don't validate here; assume input is well-formed per spec.
            for (int i = 0; i < rows; i++) {
                String line = scanner.next();
                for (int j = 0; j < cols; j++) map[i][j] = line.charAt(j);
            }

            int numPhases = scanner.nextInt();

            // BP tracks islands (black points). WP tracks white components (potential lakes).
            Partition<PositionInfo> BP = new Partition<>();
            Partition<PositionInfo> WP = new Partition<>();
            @SuppressWarnings("unchecked")
            Node<PositionInfo>[][] cluster = new Node[rows][cols];      // BP nodes per black cell
            @SuppressWarnings("unchecked")
            Node<PositionInfo>[][] whiteCluster = new Node[rows][cols]; // WP nodes per white cell

            // Phase 0: build initial partitions and report
            IslandLakeResult result = processInitialPhase(map, rows, cols, BP, WP, cluster, whiteCluster);
            printResults(result);

            // Match sample formatting: blank line only if more phases follow
            if (numPhases > 0) System.out.println();

            // Subsequent phases: add land, update BP, rebuild WP (simpler + correct), then report
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
            // Keep the error message terse so the output file doesn't get polluted
            System.err.println("Error reading input: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void printResults(IslandLakeResult result) {
        // 1) number of islands
        System.out.println(result.islandCount);

        // 2) list of island sizes (decreasing) or -1 if none
        if (result.islandSizes.isEmpty()) {
            System.out.println(-1);
        } else {
            for (int sz : result.islandSizes) System.out.println(sz);
        }

        // 3) total island area (this already includes lakes for 2B)
        System.out.println(result.totalIslandArea);

        // The assignment sample omits lake lines only in the degenerate case: no islands and no lakes.
        // Otherwise, always print both lake lines.
        if (!(result.islandCount == 0 && result.totalLakeCount == 0 && result.totalLakeArea == 0)) {
            System.out.println(result.totalLakeCount);
            System.out.println(result.totalLakeArea);
        }
    }

    // Phase 0: build BP with 4-neighbor connectivity; build WP with 8-neighbor connectivity
    private static IslandLakeResult processInitialPhase(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        // Create a singleton BP node for every black cell
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    PositionInfo pos = new PositionInfo(i, j);
                    cluster[i][j] = BP.makeCluster(pos);
                }
            }
        }
        // Union adjacent black cells (4-neighbor only). Checking right/down avoids double work.
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

        // Build WP with corner connectivity (8-neighbor). This is per the 2B hint.
        buildWhitePartitionFromScratch(map, rows, cols, WP, whiteCluster);

        return getCurrentResults(map, rows, cols, BP, WP, cluster, whiteCluster);
    }

    // New phase: add new black squares, union with adjacent blacks, rebuild WP, then compute results
    private static IslandLakeResult processNewPhase(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Node<PositionInfo>[][] cluster,
            List<PositionInfo> newPositions) {

        // Create BP nodes for new land, and flip the map to '1'
        for (PositionInfo pos : newPositions) {
            int i = pos.row, j = pos.col;
            cluster[i][j] = BP.makeCluster(pos);
            map[i][j] = '1';
        }

        // Connect new land to any side-adjacent existing land (4-neighbor)
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

        // Rebuild WP every phase. It’s simpler and avoids delicate incremental corner-cases.
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

        // First pass: create a node for every white cell
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '0') {
                    PositionInfo pos = new PositionInfo(i, j);
                    whiteCluster[i][j] = WP.makeCluster(pos);
                }
            }
        }
        // Second pass: union with all 8 neighbors to join diagonally touching whites
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

    // Aggregate everything needed for printing from BP (islands) and WP (white components)
    private static IslandLakeResult getCurrentResults(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        // Find all lakes first; we’ll add their area to the owning island leader
        List<LakeInfo> lakes = identifyLakes(map, rows, cols, BP, WP, cluster, whiteCluster);
        int totalLakeCount = lakes.size();
        int totalLakeArea  = lakes.stream().mapToInt(l -> l.area).sum();

        // Count black cells per island leader by scanning the grid; this avoids any reliance
        // on internal Partition structures and stays faithful to the ADT.
        Map<Node<PositionInfo>, Integer> baseSizes = new HashMap<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (map[i][j] == '1') {
                    Node<PositionInfo> leader = BP.find(cluster[i][j]);
                    baseSizes.merge(leader, 1, Integer::sum);
                }
            }
        }

        // Accumulate lake area by island leader so we can add it cleanly to the base sizes
        Map<Node<PositionInfo>, Integer> lakeByIsland = new HashMap<>();
        for (LakeInfo lake : lakes) {
            lakeByIsland.merge(lake.containingIsland, lake.area, Integer::sum);
        }

        // Convert per-leader sizes to a sorted list of integers (final island sizes)
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

    // Identify lakes: a WP component that (a) does not touch the map edge and (b) by SIDES touches exactly one island
    private static List<LakeInfo> identifyLakes(
            char[][] map, int rows, int cols,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP,
            Node<PositionInfo>[][] cluster, Node<PositionInfo>[][] whiteCluster) {

        List<LakeInfo> lakes = new ArrayList<>();
        // We only want to process each WP component once, so keep track by its leader
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

    // Decide if a white component is a lake. If so, return its area and the owning island leader.
    private static LakeInfo checkIfLake(
            char[][] map, int rows, int cols, Node<PositionInfo> whiteHead,
            Partition<PositionInfo> BP, Partition<PositionInfo> WP, Node<PositionInfo>[][] cluster) {

        // Get all nodes for this white component so we can check edges and adjacency thoroughly
        List<Node<PositionInfo>> whites = WP.clusterPositions(whiteHead);
        int area = whites.size();

        boolean touchesEdge = false;
        // We’ll collect distinct BP leaders that this white component touches by SIDES
        Set<Node<PositionInfo>> adjacentIslands = new HashSet<>();

        // Important: use 4-neighbor here (sides only) to test adjacency to islands (per spec)
        int[][] sideDirs = {{0,1},{1,0},{0,-1},{-1,0}};

        for (Node<PositionInfo> wn : whites) {
            PositionInfo p = WP.element(wn);
            int i = p.row, j = p.col;

            // If any cell in the component is on the border, it is not a lake
            if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1) {
                touchesEdge = true;
                break;
            }

            // Collect which island leaders are side-adjacent to this white cell
            for (int[] d : sideDirs) {
                int ni = i + d[0], nj = j + d[1];
                if (ni >= 0 && ni < rows && nj >= 0 && nj < cols && map[ni][nj] == '1') {
                    adjacentIslands.add(BP.find(cluster[ni][nj]));
                }
            }
        }

        // Lake must be fully interior and touch exactly one island by sides
        if (!touchesEdge && adjacentIslands.size() == 1) {
            Node<PositionInfo> containingIsland = adjacentIslands.iterator().next();
            return new LakeInfo(area, containingIsland);
        }
        return null;
    }
}
