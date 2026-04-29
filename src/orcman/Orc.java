package orcman;

/*
 * Author: OrcMan Project
 * Class: Orc.java
 *
 * Description:
 *   Enemy orc character. Extends Entity and adds:
 *     - Staggered release timer (orc stays frozen until releaseTime)
 *     - Pathfinding AI (greedy Manhattan-distance chase + random wander)
 *
 *   Data structure: ArrayList<Integer> used inside chooseDirection() to
 *   collect valid movement choices. ArrayList chosen over array because
 *   the number of valid directions varies (1-4) and we need index access.
 */

import java.util.ArrayList;

public class Orc extends Entity {

    // ── Release timing ───────────────────────────────────────────────────
    public boolean active;          // false = still waiting in corner
    public long    releaseTime;     // System.currentTimeMillis() threshold
    public int     index;           // 0-3, identifies this orc

    // ── Constructor ──────────────────────────────────────────────────────
    public Orc(int col, int row, int index, int level) {
        super(col, row);
        this.index       = index;
        this.active      = false;
        this.speed       = 70 + index * 8 + (level - 1) * 5;
        this.frameDur    = 130;
        this.frameCount  = 8;
        // Top-row orcs start moving down; bottom-row start moving up
        this.dir         = (index < 2) ? DOWN : UP;
        this.facing      = (index == 1 || index == 3) ? -1 : 1;
    }

    /*
     * chooseDirection()
     * Greedy AI: 65% of the time picks the direction that minimises
     * Manhattan distance to the player. 35% picks randomly to avoid
     * the orc getting permanently stuck behind walls.
     * Orcs are also prevented from reversing 180° unless forced.
     *
     * Algorithm (Greedy with random scatter):
     *   1. Collect all valid neighbouring tiles (not wall, not reverse).
     *   2. If none found, allow the reverse direction as fallback.
     *   3. With 65% probability: pick the tile with lowest
     *      |col - playerCol| + |row - playerRow| (Manhattan distance).
     *   4. Otherwise: pick a random valid direction.
     *
     * Time Complexity: O(4) = O(1) -- at most 4 directions to check.
     *
     * Data structure used: ArrayList<Integer> for valid direction list.
     */
    public void chooseDirection(int[][] maze, double playerX, double playerY) {
        int oc  = Tile.toCol(x);
        int or_ = Tile.toRow(y);

        // Only decide at tile centres to keep movement grid-aligned
        if (Math.abs(x - Tile.centrePx(oc)) >= 2) return;
        if (Math.abs(y - Tile.centrePy(or_)) >= 2) return;

        // Snap to centre
        x = Tile.centrePx(oc);
        y = Tile.centrePy(or_);

        int reverse = (dir + 2) % 4;

        // Build lists of valid directions
        ArrayList<Integer> valid = new ArrayList<>();
        ArrayList<Integer> any   = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int nc = oc + DIRS[i][0];
            int nr = or_ + DIRS[i][1];
            if (!Tile.isWall(maze, nc, nr)) {
                any.add(i);
                if (i != reverse) valid.add(i);
            }
        }

        ArrayList<Integer> choices = valid.isEmpty() ? any : valid;
        if (choices.isEmpty()) return;

        int playerCol = Tile.toCol(playerX);
        int playerRow = Tile.toRow(playerY);

        if (Math.random() < 0.65) {
            // Greedy: choose direction closest to player (Manhattan)
            int best     = choices.get(0);
            int bestDist = Integer.MAX_VALUE;
            for (int di : choices) {
                int nc   = oc + DIRS[di][0];
                int nr   = or_ + DIRS[di][1];
                int dist = Math.abs(nc - playerCol) + Math.abs(nr - playerRow);
                if (dist < bestDist) {
                    bestDist = dist;
                    best     = di;
                }
            }
            dir = best;
        } else {
            // Random scatter
            dir = choices.get((int)(Math.random() * choices.size()));
        }

        if (DIRS[dir][0] != 0) facing = DIRS[dir][0];
    }
}
