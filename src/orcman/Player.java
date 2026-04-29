package orcman;

/*
 * Author: OrcMan Project
 * Class: Player.java
 *
 * Description:
 *   The human-controlled soldier character.
 *   Extends Entity and adds queued direction input, sword inventory,
 *   and the sword-swing attack.
 *
 *   Data structure: int (swordsHeld) acts as a simple stack counter --
 *   pick up adds to the top, attacking pops one off.
 *   A full Stack<Sword> would be overkill here but the LIFO logic is
 *   conceptually the same.
 */

import java.util.Stack;

public class Player extends Entity {

    // ── Input ────────────────────────────────────────────────────────────
    public int nextDir;     // direction queued by the most recent key press

    // ── Sword inventory (Stack -- last picked up = first used) ───────────
    /*
     * Data structure: Stack<Integer>
     * Chosen because sword use is LIFO: the last sword picked up is the
     * first one consumed. Stack push/pop are both O(1).
     */
    private Stack<Integer> swordStack;

    // ── Attack state ─────────────────────────────────────────────────────
    public boolean attackActive;
    public int     attackTimer;         // ms remaining in swing animation
    public static final int ATTACK_DUR   = 320;  // ms the swing lasts
    public static final int ATTACK_RANGE = 72;   // pixel radius

    // ── Constructor ──────────────────────────────────────────────────────
    public Player(int col, int row) {
        super(col, row);
        this.nextDir     = RIGHT;
        this.speed       = 115;
        this.frameDur    = 95;
        this.frameCount  = 8;
        this.swordStack  = new Stack<>();
        this.attackActive = false;
        this.attackTimer  = 0;
    }

    /*
     * tryTurn()
     * Attempts to apply the queued direction before moving.
     * Only commits the turn if the tile in the new direction is open,
     * and aligns the entity to the lane first to avoid clipping.
     *
     * Algorithm:
     *   1. Read queued direction.
     *   2. Compute neighbour tile in that direction.
     *   3. If open: align perpendicular axis, commit direction.
     *   4. If not open: keep current direction.
     * O(1)
     */
    public void tryTurn(int[][] maze) {
        int[] nd = DIRS[nextDir];
        int tc   = Tile.toCol(x);
        int tr   = Tile.toRow(y);

        if (!Tile.isWall(maze, tc + nd[0], tr + nd[1])) {
            if (nd[0] != 0) y = Tile.centrePy(tr);  // horizontal turn: snap y
            if (nd[1] != 0) x = Tile.centrePx(tc);  // vertical turn:   snap x
            dir = nextDir;
            if (nd[0] != 0) facing = nd[0];
        }
    }

    // ── Sword inventory helpers ──────────────────────────────────────────
    public void pickUpSword()  { swordStack.push(1); }
    public int  swordsHeld()   { return swordStack.size(); }
    public boolean hasSword()  { return !swordStack.isEmpty(); }

    /*
     * startAttack()
     * Consumes one sword from the stack and activates the swing animation.
     * Returns false if no swords are held.
     * O(1) -- Stack.pop() is O(1).
     */
    public boolean startAttack() {
        if (!hasSword() || attackActive) return false;
        swordStack.pop();
        attackActive = true;
        attackTimer  = ATTACK_DUR;
        return true;
    }

    /*
     * updateAttack()
     * Counts down the attack timer each tick.
     * O(1).
     */
    public void updateAttack(int dt) {
        if (attackActive) {
            attackTimer -= dt;
            if (attackTimer <= 0) attackActive = false;
        }
    }
}
