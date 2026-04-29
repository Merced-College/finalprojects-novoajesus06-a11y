package orcman;

/*
 * Author: OrcMan Project
 * Class: Dot.java
 *
 * Description:
 *   Represents a single collectible on the maze floor.
 *   A Dot is either a coin (worth 10 pts) or a sword pickup.
 *   All dots for a level are stored in a LinkedList<Dot> in Level.java.
 *
 *   Data structure note: Dot objects are nodes stored in a LinkedList.
 *   LinkedList chosen so eaten dots can be removed in O(1) via iterator,
 *   avoiding the O(n) shift cost of ArrayList removal mid-traversal.
 */

public class Dot {

    // ── Fields ───────────────────────────────────────────────────────────────
    public final int col;       // maze column
    public final int row;       // maze row
    public boolean eaten;       // true once the player has collected this dot
    public boolean isSword;     // true if this pickup is a sword (not a coin)

    // Pixel centre (cached for fast collision checks)
    public final double px;
    public final double py;

    public static final int COIN_SCORE = 10;

    // ── Constructor ──────────────────────────────────────────────────────────
    public Dot(int col, int row) {
        this.col    = col;
        this.row    = row;
        this.eaten  = false;
        this.isSword = false;
        this.px     = Tile.centrePx(col);
        this.py     = Tile.centrePy(row);
    }

    /*
     * overlaps()
     * Returns true if the given entity pixel position is close enough
     * to collect this dot.
     *
     * Algorithm: Euclidean distance squared vs. threshold squared.
     * Using squared distance avoids a sqrt -- O(1).
     */
    public boolean overlaps(double entityPx, double entityPy) {
        double dx = entityPx - px;
        double dy = entityPy - py;
        double threshSq = (Tile.SIZE * 0.45) * (Tile.SIZE * 0.45);
        return (dx * dx + dy * dy) < threshSq;
    }
}
