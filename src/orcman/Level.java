package orcman;

/*
 * Author: OrcMan Project
 * Class: Level.java
 *
 * Description:
 *   Owns everything that belongs to a single level:
 *     - The maze grid (2D array)
 *     - The dot/sword list (LinkedList<Dot>)
 *     - The four Orc instances
 *     - The Player instance
 *   Also handles dot collection, sword placement, and orc release timing.
 *
 *   Data structures used:
 *     - int[][] maze       : 2D array for O(1) wall lookup
 *     - LinkedList<Dot>    : allows O(1) iterator removal of eaten dots
 *     - Orc[]  orcs        : fixed-size array; 4 orcs, index = identity
 */

import java.util.Collections;
import java.util.LinkedList;
import java.util.ArrayList;

public class Level {

    // ── Constants ────────────────────────────────────────────────────────
    private static final int[] ORC_RELEASE_MS = {0, 5000, 10000, 15000};
    private static final int[][] CORNER_TILES  = {{1,1},{18,1},{1,13},{18,13}};

    // ── State ────────────────────────────────────────────────────────────
    public int[][]          maze;
    public LinkedList<Dot>  dots;    // LinkedList: O(1) remove during iteration
    public Orc[]            orcs;    // fixed array: always exactly 4 orcs
    public Player           player;
    public int              number;  // which level this is (1-based)

    // ── Constructor ──────────────────────────────────────────────────────
    public Level(int levelNumber) {
        this.number = levelNumber;
        maze   = Tile.freshMaze();
        dots   = new LinkedList<>();
        orcs   = new Orc[4];
        buildDots(levelNumber);
        buildOrcs(levelNumber);
        player = new Player(9, 7);
    }

    /*
     * buildDots()
     * Iterates every open tile, creates a Dot, then randomly promotes
     * some dots to sword pickups.
     *
     * Algorithm:
     *   1. Scan all ROWS x COLS tiles -- add a Dot for each PATH tile.
     *   2. Shuffle candidate list (Fisher-Yates via Collections.shuffle).
     *   3. Mark first swordCount candidates as sword pickups.
     *
     * Sword count formula: max(0, 5 - (level-1))
     *   Level 1 → 5 swords, Level 2 → 4, ..., Level 6+ → 0.
     *
     * Time Complexity: O(ROWS * COLS) for scan + O(n) for shuffle.
     */
    private void buildDots(int levelNumber) {
        for (int r = 0; r < Tile.ROWS; r++) {
            for (int c = 0; c < Tile.COLS; c++) {
                if (maze[r][c] == Tile.PATH) {
                    dots.add(new Dot(c, r));
                }
            }
        }

        // Exclude the player start tile from sword candidates
        ArrayList<Dot> candidates = new ArrayList<>();
        for (Dot d : dots) {
            if (!(d.col == 9 && d.row == 7)) candidates.add(d);
        }

        Collections.shuffle(candidates);

        int swordCount = Math.max(0, 5 - (levelNumber - 1));
        for (int i = 0; i < Math.min(swordCount, candidates.size()); i++) {
            candidates.get(i).isSword = true;
        }
    }

    /*
     * buildOrcs()
     * Creates the four orcs at the four maze corners with staggered
     * release times so they don't all rush the player at once.
     */
    private void buildOrcs(int levelNumber) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            int col = CORNER_TILES[i][0];
            int row = CORNER_TILES[i][1];
            orcs[i] = new Orc(col, row, i, levelNumber);
            orcs[i].releaseTime = now + ORC_RELEASE_MS[i];
        }
    }

    /*
     * update()
     * Main per-tick logic for the level.
     * Returns a LevelResult indicating what happened this tick.
     *
     * Algorithm:
     *   1. If player is dead, count down deathTimer; return PLAYER_DIED
     *      when timer expires.
     *   2. Update player attack timer.
     *   3. Apply player turn request, then move player.
     *   4. Collect any dot the player is standing on.
     *   5. If all dots eaten → return LEVEL_CLEAR.
     *   6. For each active orc: let it choose direction, move it,
     *      check collision with player.
     *   7. Return RUNNING.
     *
     * Time Complexity: O(D + N) where D = remaining dots, N = orc count (4).
     */
    public LevelResult update(int dt) {
        long now = System.currentTimeMillis();

        // ── Player death cooldown ────────────────────────────────────────
        if (player.dead) {
            player.deathTimer -= dt;
            if (player.deathTimer <= 0) return LevelResult.PLAYER_DIED;
            return LevelResult.RUNNING;
        }

        // ── Attack timer ─────────────────────────────────────────────────
        player.updateAttack(dt);

        // ── Player movement ──────────────────────────────────────────────
        player.tryTurn(maze);
        player.move(maze, dt);

        // ── Dot collection ───────────────────────────────────────────────
        // Use iterator so we can remove eaten dots from the LinkedList in O(1)
        java.util.Iterator<Dot> it = dots.iterator();
        while (it.hasNext()) {
            Dot d = it.next();
            if (!d.eaten && d.overlaps(player.x, player.y)) {
                d.eaten = true;
                if (d.isSword) {
                    player.pickUpSword();
                }
                it.remove();   // O(1) LinkedList removal
            }
        }

        // ── Level clear check ────────────────────────────────────────────
        if (dots.isEmpty()) return LevelResult.LEVEL_CLEAR;

        // ── Orcs ─────────────────────────────────────────────────────────
        for (Orc orc : orcs) {
            if (orc.dead) {
                orc.deathTimer -= dt;
                continue;
            }
            if (!orc.active) {
                if (now >= orc.releaseTime) orc.active = true;
                else continue;
            }

            orc.chooseDirection(maze, player.x, player.y);
            orc.move(maze, dt);

            // Kill player on contact
            if (orc.collidesWith(player) && !player.dead) {
                player.dead        = true;
                player.deathTimer  = 1800;
            }

            // Sword attack kills orc
            if (player.attackActive) {
                double dx   = orc.x - player.x;
                double dy   = orc.y - player.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < Player.ATTACK_RANGE) {
                    orc.dead        = true;
                    orc.deathTimer  = 800;
                }
            }
        }

        return LevelResult.RUNNING;
    }

    // Score for killing all visible (non-dead) orcs counted externally
    public int countAliveOrcs() {
        int n = 0;
        for (Orc o : orcs) if (!o.dead) n++;
        return n;
    }
}
