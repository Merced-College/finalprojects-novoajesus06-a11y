package orcman;

/*
 * Author: OrcMan Project
 * Class: Tile.java
 *
 * Description:
 *   Stores the maze layout as a 2D int array (data structure: 2D array /
 *   matrix) and exposes helper constants and lookup methods.
 *
 *   Data structure: 2D array (int[][])
 *   Chosen because the maze is a fixed-size grid -- row/col access is O(1),
 *   and a 2D array maps directly to screen tile coordinates with no overhead.
 */

public class Tile {

    public static final int WALL = 1;
    public static final int PATH = 0;

    public static final int COLS = 20;
    public static final int ROWS = 15;
    public static final int SIZE = 40;   // pixel width/height of each tile

    /*
     * LAYOUT
     * The maze definition. 1 = wall, 0 = open path.
     * 20 columns x 15 rows -- matches canvas size 800x600.
     *
     * Data structure: 2D int array
     * Each call to Tile.freshMaze() deep-copies this so each level
     * gets its own independent grid.
     */
    private static final int[][] LAYOUT = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1},
        {1,0,1,1,0,1,1,1,0,1,1,0,1,1,1,0,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,0,1},
        {1,0,0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0,1},
        {1,1,1,1,0,1,1,1,0,0,0,0,1,1,1,0,1,1,1,1},
        {1,1,1,1,0,1,0,0,0,1,1,0,0,0,1,0,1,1,1,1},
        {1,0,0,0,0,0,0,1,0,1,1,0,1,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,0,1,0,0,0,0,1,0,1,0,1,1,0,1},
        {1,0,0,1,0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,1},
        {1,1,0,1,0,1,1,1,0,1,1,0,1,1,1,0,1,0,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    /*
     * freshMaze()
     * Returns a deep copy of LAYOUT so each level has its own grid.
     * Algorithm: nested loop deep-copy -- O(ROWS * COLS).
     */
    public static int[][] freshMaze() {
        int[][] copy = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            copy[r] = java.util.Arrays.copyOf(LAYOUT[r], COLS);
        }
        return copy;
    }

    /*
     * isWall()
     * Bounds-checked wall query. Returns true if out of bounds (treat as wall).
     * O(1) lookup.
     */
    public static boolean isWall(int[][] maze, int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return true;
        return maze[row][col] == WALL;
    }

    // Pixel centre of a tile
    public static double centrePx(int col) { return col * SIZE + SIZE / 2.0; }
    public static double centrePy(int row) { return row * SIZE + SIZE / 2.0; }

    // Nearest tile column/row from a pixel centre coordinate
    public static int toCol(double px) { return (int) Math.round((px - SIZE / 2.0) / SIZE); }
    public static int toRow(double py) { return (int) Math.round((py - SIZE / 2.0) / SIZE); }
}
