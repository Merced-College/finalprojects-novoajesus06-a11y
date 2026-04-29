package orcman;

/*
 * Author: OrcMan Project
 * Class: GamePanel.java
 *
 * Description:
 *   The main Swing JPanel. Owns the game loop (javax.swing.Timer at 60fps),
 *   handles keyboard input, drives the screen state machine, and delegates
 *   all drawing to Renderer and all simulation to Level.
 *
 *   Screen states: TITLE → PLAY → GAME_OVER → PLAY (loop)
 *
 *   Data structure: HashMap<Integer,Boolean> for key state tracking.
 *   HashMap gives O(1) key-down lookup by keyCode.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ── Layout constants ──────────────────────────────────────────────────
    public  static final int UI_H = 44;   // height of the HUD bar above the maze
    private static final int W    = Tile.COLS * Tile.SIZE;
    private static final int H    = Tile.ROWS * Tile.SIZE;

    // ── Screen state ──────────────────────────────────────────────────────
    private static final int SCREEN_TITLE = 0;
    private static final int SCREEN_PLAY  = 1;
    private static final int SCREEN_OVER  = 2;
    private int screen = SCREEN_TITLE;

    // ── Game objects ──────────────────────────────────────────────────────
    private Level      level;
    private ScoreBoard scoreBoard;
    private Renderer   renderer;

    // ── Session counters ──────────────────────────────────────────────────
    private int lives = 3;
    private int score = 0;
    private int levelNum = 1;

    // ── Transition banner ─────────────────────────────────────────────────
    private boolean transitioning = false;
    private javax.swing.Timer transTimer;

    // ── Input ─────────────────────────────────────────────────────────────
    /*
     * Data structure: HashMap<Integer,Boolean>
     * Maps keyCode → pressed state. O(1) insert and lookup.
     * Lets any number of keys be tracked simultaneously without a fixed array.
     */
    private HashMap<Integer, Boolean> keys = new HashMap<>();

    // ── Game loop timer ───────────────────────────────────────────────────
    private javax.swing.Timer gameTimer;
    private long lastTick;

    // ── Constructor ───────────────────────────────────────────────────────
    public GamePanel() {
        setPreferredSize(new Dimension(W, H + UI_H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        renderer   = new Renderer();
        scoreBoard = new ScoreBoard();

        // Bootstrap a dummy level so the title screen can draw the maze
        level = new Level(1);

        gameTimer = new javax.swing.Timer(16, this);   // ~60 fps
        gameTimer.start();
        lastTick = System.currentTimeMillis();
    }

    // ── Game loop (called by Swing timer every ~16 ms) ────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        int  dt  = (int) Math.min(now - lastTick, 50);
        lastTick = now;

        if (screen == SCREEN_PLAY && !transitioning) {
            tick(dt);
        }
        repaint();
    }

    /*
     * tick()
     * Advances simulation one frame.
     *
     * Algorithm:
     *   1. Delegate to Level.update(dt).
     *   2. On PLAYER_DIED: decrement lives; if 0 → game over, else respawn.
     *   3. On LEVEL_CLEAR: show banner, pause briefly, then load next level.
     *   4. Add orc-kill score inside Level (200 pts per kill tracked here).
     */
    private void tick(int dt) {
        // Mirror key state to player direction
        applyKeys();

        // Count orcs alive before update to detect kills
        int orcsBefore = level.countAliveOrcs();

        LevelResult result = level.update(dt);

        // Award 200 pts per orc killed this tick
        int orcsAfter  = level.countAliveOrcs();
        score += (orcsBefore - orcsAfter) * 200;

        // Award dot score: count eaten dots delta tracked via Level
        // (Dots award 10 pts; accounted here each tick by checking removal)
        // Simpler: Level can return score delta. We use a callback-free
        // approach: score is incremented outside via dot events in Level.
        // Because Level owns dots and removes them, we track via a snapshot.
        // For simplicity we poll: total possible dots - remaining.
        // (See addDotScore helper below)
        addDotScore();

        switch (result) {
            case PLAYER_DIED:
                lives--;
                if (lives <= 0) {
                    scoreBoard.add(score);
                    screen = SCREEN_OVER;
                } else {
                    respawn();
                }
                break;

            case LEVEL_CLEAR:
                levelNum++;
                transitioning = true;
                transTimer = new javax.swing.Timer(1000, ev -> {
                    level = new Level(levelNum);
                    dotsEatenSnapshot = 0;
                    transitioning = false;
                    ((javax.swing.Timer) ev.getSource()).stop();
                });
                transTimer.setRepeats(false);
                transTimer.start();
                break;

            case RUNNING:
            default:
                break;
        }
    }

    // ── Dot score tracking ────────────────────────────────────────────────
    // We track how many dots have been eaten by comparing list size to last tick.
    private int dotsLastSize  = -1;
    private int dotsTotalAtStart = 0;
    private int dotsCounted   = 0;    // dots for which we've already awarded pts
    private int dotsCreditedScore = 0;
    private int dotsEatenSnapshot = 0;

    private void addDotScore() {
        if (level == null) return;
        int remaining = level.dots.size();
        // We know total open tiles at level start minus swords
        // Simple: track dots eaten as total_at_start - remaining
        // initialise snapshot on first call
        if (dotsLastSize < 0) {
            dotsLastSize     = remaining;
            dotsCreditedScore = 0;
            return;
        }
        int newlyEaten = dotsLastSize - remaining;
        if (newlyEaten > 0) {
            score += newlyEaten * Dot.COIN_SCORE;
            dotsLastSize = remaining;
        }
    }

    private void respawn() {
        level = new Level(levelNum);
        dotsLastSize = -1;
    }

    // ── Key → player direction ────────────────────────────────────────────
    private void applyKeys() {
        if (level == null || level.player == null) return;
        Player p = level.player;
        if (isDown(KeyEvent.VK_UP)    || isDown(KeyEvent.VK_W)) p.nextDir = Entity.UP;
        if (isDown(KeyEvent.VK_RIGHT) || isDown(KeyEvent.VK_D)) p.nextDir = Entity.RIGHT;
        if (isDown(KeyEvent.VK_DOWN)  || isDown(KeyEvent.VK_S)) p.nextDir = Entity.DOWN;
        if (isDown(KeyEvent.VK_LEFT)  || isDown(KeyEvent.VK_A)) p.nextDir = Entity.LEFT;
    }

    private boolean isDown(int code) {
        return Boolean.TRUE.equals(keys.get(code));
    }

    // ── Start / restart ───────────────────────────────────────────────────
    private void startGame() {
        lives       = 3;
        score       = 0;
        levelNum    = 1;
        dotsLastSize = -1;
        level       = new Level(levelNum);
        transitioning = false;
        screen      = SCREEN_PLAY;
    }

    // ── Paint ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD at the top
        renderer.drawHUD(g2, lives, score, levelNum,
                         level != null ? level.player.swordsHeld() : 0,
                         getWidth());

        // Push down by UI_H for the game canvas
        g2.translate(0, UI_H);

        if (screen == SCREEN_TITLE) {
            renderer.drawTitle(g2, scoreBoard.getHighScore());
        } else if (screen == SCREEN_PLAY) {
            renderer.drawGame(g2, level, score, lives, levelNum, transitioning);
        } else if (screen == SCREEN_OVER) {
            renderer.drawGame(g2, level, score, lives, levelNum, false);
            renderer.drawGameOver(g2, score, scoreBoard.getHighScore());
        }
    }

    // ── KeyListener ───────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        keys.put(k, true);

        // Screen transitions on ENTER
        if (k == KeyEvent.VK_ENTER) {
            if (screen == SCREEN_TITLE || screen == SCREEN_OVER) {
                startGame();
            }
        }

        // Attack on SPACE (in-game only)
        if (k == KeyEvent.VK_SPACE && screen == SCREEN_PLAY
                && level != null && !transitioning) {
            level.player.startAttack();
        }
    }

    @Override public void keyReleased(KeyEvent e) { keys.put(e.getKeyCode(), false); }
    @Override public void keyTyped(KeyEvent e) {}
}
