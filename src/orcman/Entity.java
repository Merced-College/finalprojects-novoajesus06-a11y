package orcman;

/*
 * Author: OrcMan Project
 * Class: Entity.java
 *
 * Description:
 *   Abstract base class for every moving object in the game (Player, Orc).
 *   Holds shared position, direction, speed, and animation state.
 *   Demonstrates abstraction / inheritance (SLO D & E).
 *
 *   The shared move() algorithm lives here so Player and Orc do not
 *   duplicate the wall-collision logic.
 */

public abstract class Entity {

    // ── Direction constants (index into DIRS array) ───────────────────────
    public static final int UP    = 0;
    public static final int RIGHT = 1;
    public static final int DOWN  = 2;
    public static final int LEFT  = 3;

    // Direction vectors: {dx, dy} for UP / RIGHT / DOWN / LEFT
    public static final int[][] DIRS = {
        { 0, -1},   // UP
        { 1,  0},   // RIGHT
        { 0,  1},   // DOWN
        {-1,  0}    // LEFT
    };

    // ── Position & movement ──────────────────────────────────────────────
    public double x, y;         // pixel centre
    public int    dir;          // current direction (UP/RIGHT/DOWN/LEFT)
    public int    facing;       // 1 = right, -1 = left (for sprite flip)
    public double speed;        // pixels per second

    // ── Animation ────────────────────────────────────────────────────────
    public int frame;           // current sprite frame index
    public int frameTimer;      // ms since last frame advance
    public int frameDur;        // ms per frame
    public int frameCount;      // total frames in current animation

    // ── Death state ──────────────────────────────────────────────────────
    public boolean dead;
    public int     deathTimer;  // ms remaining in death animation

    // ── Constructor ──────────────────────────────────────────────────────
    public Entity(int col, int row) {
        this.x          = Tile.centrePx(col);
        this.y          = Tile.centrePy(row);
        this.dir        = RIGHT;
        this.facing     = 1;
        this.speed      = 100;
        this.frame      = 0;
        this.frameTimer = 0;
        this.frameDur   = 110;
        this.frameCount = 8;
        this.dead       = false;
        this.deathTimer = 0;
    }

    /*
     * move()
     * Advances the entity one game tick along its current direction.
     * Wall collision is checked via Tile.isWall().
     *
     * Algorithm:
     *   1. Compute candidate next position using speed * dt.
     *   2. Convert candidate pixel to tile coords.
     *   3. If tile is open: accept move, clamp to lane axis.
     *   4. If tile is wall: snap entity back to current tile centre.
     *
     * Time Complexity: O(1)
     */
    public void move(int[][] maze, int dt) {
        // Advance animation frame
        frameTimer += dt;
        if (frameTimer >= frameDur) {
            frameTimer = 0;
            frame = (frame + 1) % frameCount;
        }

        int[] d  = DIRS[dir];
        double nx = x + d[0] * speed * dt / 1000.0;
        double ny = y + d[1] * speed * dt / 1000.0;

        int nc = Tile.toCol(nx);
        int nr = Tile.toRow(ny);

        if (!Tile.isWall(maze, nc, nr)) {
            x = nx;
            y = ny;
            // Clamp to lane: keep the perpendicular axis locked to tile centre
            int tc = Tile.toCol(x);
            int tr = Tile.toRow(y);
            if (d[0] != 0) y = Tile.centrePy(tr);   // moving horizontally: lock y
            if (d[1] != 0) x = Tile.centrePx(tc);   // moving vertically:   lock x
        } else {
            // Snap back to tile centre so entity never clips into a wall
            x = Tile.centrePx(Tile.toCol(x));
            y = Tile.centrePy(Tile.toRow(y));
        }
    }

    /*
     * collidesWith()
     * Euclidean distance check used for player-vs-orc collision.
     * Returns true if the two entities overlap within TILE*0.68 radius.
     * O(1).
     */
    public boolean collidesWith(Entity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double threshold = Tile.SIZE * 0.68;
        return Math.sqrt(dx * dx + dy * dy) < threshold;
    }
}
