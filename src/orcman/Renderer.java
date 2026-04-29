package orcman;

/*
 * Author: OrcMan Project
 * Class: Renderer.java
 *
 * Description:
 *   Pure drawing class. Receives a Graphics2D context and the current game
 *   state and paints everything: background, maze walls, dots, player,
 *   orcs, attack arc, HUD, overlays.
 *
 *   Separating rendering from logic follows the principle of separation of
 *   concerns (SLO D/F) -- GamePanel calls Renderer; it knows nothing about
 *   Swing timers or key events.
 */

import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer {

    // Pixel dimensions
    private static final int W = Tile.COLS * Tile.SIZE;
    private static final int H = Tile.ROWS * Tile.SIZE;

    // ── Public draw entry points ──────────────────────────────────────────

    /*
     * drawGame()
     * Draws one complete frame of the in-play state.
     */
    public void drawGame(Graphics2D g, Level level, int score, int lives,
                         int levelNum, boolean transitioning) {
        drawBackground(g);
        drawMaze(g, level.maze);
        drawDots(g, level.dots);
        drawOrcs(g, level.orcs);
        drawPlayer(g, level.player);
        if (level.player.attackActive) drawAttackArc(g, level.player);
        if (transitioning) drawLevelBanner(g, levelNum);
    }

    /*
     * drawTitle()
     * Title / start screen.
     */
    public void drawTitle(Graphics2D g, int highScore) {
        drawBackground(g);
        // dark overlay
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, W, H);

        int cx = W / 2, cy = H / 2;
        drawPanel(g, cx - 230, cy - 130, 460, 280);

        g.setColor(new Color(255, 215, 0));
        drawCentredString(g, "ORCMAN", cx, cy - 68,
                new Font("Monospaced", Font.BOLD, 52));

        g.setColor(new Color(200, 200, 255));
        Font small = new Font("Monospaced", Font.PLAIN, 15);
        drawCentredString(g, "Arrow keys / WASD  \u2014  move",            cx, cy - 14, small);
        drawCentredString(g, "SPACE  \u2014  swing sword (\u2694 pick up first!)", cx, cy + 10, small);
        drawCentredString(g, "ENTER  \u2014  start",                        cx, cy + 34, small);

        if (highScore > 0) {
            g.setColor(new Color(255, 215, 0));
            drawCentredString(g, "Best: " + highScore, cx, cy + 70,
                    new Font("Monospaced", Font.BOLD, 18));
        }
    }

    /*
     * drawGameOver()
     * Game-over overlay drawn on top of the frozen game frame.
     */
    public void drawGameOver(Graphics2D g, int score, int highScore) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, W, H);

        int cx = W / 2, cy = H / 2;
        drawPanel(g, cx - 210, cy - 110, 420, 230);

        g.setColor(new Color(255, 60, 60));
        drawCentredString(g, "GAME OVER", cx, cy - 44,
                new Font("Monospaced", Font.BOLD, 44));

        g.setColor(new Color(255, 215, 0));
        drawCentredString(g, "Score: " + score, cx, cy + 4,
                new Font("Monospaced", Font.BOLD, 24));

        if (highScore > 0) {
            g.setColor(new Color(180, 255, 180));
            drawCentredString(g, "Best:  " + highScore, cx, cy + 32,
                    new Font("Monospaced", Font.PLAIN, 18));
        }

        g.setColor(new Color(160, 200, 255));
        drawCentredString(g, "Press ENTER to play again", cx, cy + 80,
                new Font("Monospaced", Font.PLAIN, 15));
    }

    // ── HUD (drawn by GamePanel above the canvas) ─────────────────────────

    public void drawHUD(Graphics2D g, int lives, int score, int levelNum,
                        int swords, int panelW) {
        g.setColor(new Color(10, 10, 20));
        g.fillRect(0, 0, panelW, GamePanel.UI_H);
        g.setColor(new Color(30, 54, 104));
        g.drawLine(0, GamePanel.UI_H - 1, panelW, GamePanel.UI_H - 1);

        Font f = new Font("Monospaced", Font.BOLD, 18);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();

        // Lives
        g.setColor(new Color(255, 80, 80));
        g.drawString("\u2665 " + lives, 14, 30);

        // Score (centred)
        g.setColor(new Color(255, 215, 0));
        String sc = "SCORE " + score;
        g.drawString(sc, (panelW - fm.stringWidth(sc)) / 2, 30);

        // Level
        g.setColor(new Color(100, 200, 255));
        String lv = "LVL " + levelNum;
        g.drawString(lv, panelW - fm.stringWidth(lv) - 14, 30);

        // Sword icons in top-right area
        if (swords > 0 && Assets.swordItem != null) {
            int sx = panelW - fm.stringWidth(lv) - 28;
            for (int i = 0; i < swords; i++) {
                g.drawImage(Assets.swordItem, sx - 20 - i * 18, 8, 16, 16, null);
            }
            g.setColor(new Color(180, 255, 180, 220));
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g.drawString("[SPACE]", 14, GamePanel.UI_H - 5);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void drawBackground(Graphics2D g) {
        if (Assets.background != null) {
            Composite orig = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
            g.drawImage(Assets.background, 0, 0, W, H, null);
            g.setComposite(orig);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, W, H);
        }
    }

    private void drawMaze(Graphics2D g, int[][] maze) {
        for (int r = 0; r < Tile.ROWS; r++) {
            for (int c = 0; c < Tile.COLS; c++) {
                if (maze[r][c] == Tile.WALL) {
                    g.setColor(new Color(21, 32, 56));
                    g.fillRect(c * Tile.SIZE, r * Tile.SIZE, Tile.SIZE, Tile.SIZE);
                    g.setColor(new Color(30, 54, 104));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRect(c * Tile.SIZE + 1, r * Tile.SIZE + 1,
                               Tile.SIZE - 2, Tile.SIZE - 2);
                }
            }
        }
    }

    private void drawDots(Graphics2D g, java.util.LinkedList<Dot> dots) {
        for (Dot d : dots) {
            int dx = (int) d.px;
            int dy = (int) d.py;
            if (d.isSword && Assets.swordItem != null) {
                g.drawImage(Assets.swordItem, dx - 14, dy - 14, 28, 28, null);
            } else {
                g.setColor(new Color(255, 215, 0, 60));
                g.fillOval(dx - 7, dy - 7, 14, 14);
                g.setColor(new Color(255, 215, 0));
                g.fillOval(dx - 4, dy - 4, 9, 9);
            }
        }
    }

    private void drawPlayer(Graphics2D g, Player p) {
        int px = (int) p.x, py = (int) p.y;
        if (p.dead) {
            if ((System.currentTimeMillis() / 120) % 2 == 0) {
                g.setColor(new Color(255, 60, 60, 180));
                g.fillOval(px - 22, py - 22, 44, 44);
            }
            return;
        }
        BufferedImage[] frames = Assets.soldierWalkFrames;
        if (frames != null && frames.length > 0) {
            drawSpriteScaled(g, frames[p.frame % frames.length],
                             px, py, p.facing, 0.72);
        } else {
            g.setColor(new Color(70, 160, 255));
            g.fillOval(px - 18, py - 18, 36, 36);
        }
    }

    private void drawOrcs(Graphics2D g, Orc[] orcs) {
        for (Orc o : orcs) {
            if (o.dead && o.deathTimer <= 0) continue;

            float alpha = 1f;
            if (o.dead)       alpha = Math.max(0, o.deathTimer / 800f);
            else if (!o.active) alpha = 0.28f;

            Composite orig = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int ox = (int) o.x, oy = (int) o.y;
            BufferedImage[] frames = Assets.orcWalkFrames;
            if (frames != null && frames.length > 0) {
                drawSpriteScaled(g, frames[o.frame % frames.length],
                                 ox, oy, o.facing, 0.70);
            } else {
                g.setColor(new Color(180, 80, 40));
                g.fillOval(ox - 18, oy - 18, 36, 36);
            }
            g.setComposite(orig);
        }
    }

    /*
     * drawAttackArc()
     * Renders a translucent arc in the player's facing direction to
     * visualise the sword swing radius.
     */
    private void drawAttackArc(Graphics2D g, Player p) {
        int px = (int) p.x, py = (int) p.y;
        float prog  = 1f - p.attackTimer / (float) Player.ATTACK_DUR;
        float alpha = 0.5f * (1f - prog);

        Composite orig = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(255, 235, 80));

        int r          = Player.ATTACK_RANGE;
        int startAngle = p.facing > 0 ? -60 : 120;
        g.fillArc(px - r, py - r, r * 2, r * 2, startAngle, 120);

        if (Assets.swordItem != null) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                       0.9f * (1f - prog)));
            int sx = px + (int)(p.facing * r * 0.65);
            g.drawImage(Assets.swordItem, sx - 12, py - 12, 24, 24, null);
        }
        g.setComposite(orig);
    }

    private void drawLevelBanner(Graphics2D g, int levelNum) {
        int cx = W / 2, cy = H / 2;
        drawPanel(g, cx - 170, cy - 45, 340, 90);
        g.setColor(new Color(255, 215, 0));
        drawCentredString(g, "LEVEL " + levelNum, cx, cy + 14,
                new Font("Monospaced", Font.BOLD, 34));
    }

    // ── Utility ───────────────────────────────────────────────────────────

    /*
     * drawSpriteScaled()
     * Draws a BufferedImage centred at (cx, cy), scaled to `scale`,
     * flipped horizontally when facing == -1.
     */
    private void drawSpriteScaled(Graphics2D g, BufferedImage img,
                                   int cx, int cy, int facing, double scale) {
        int dw = (int)(img.getWidth()  * scale);
        int dh = (int)(img.getHeight() * scale);
        if (facing < 0) {
            // Flip: draw from right edge leftward
            g.drawImage(img, cx + dw / 2, cy - dh / 2, -dw, dh, null);
        } else {
            g.drawImage(img, cx - dw / 2, cy - dh / 2,  dw, dh, null);
        }
    }

    private void drawPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0, 0, 0, 185));
        g.fillRoundRect(x, y, w, h, 20, 20);
        g.setColor(new Color(50, 80, 140));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 20, 20);
    }

    private void drawCentredString(Graphics2D g, String s, int cx, int y, Font f) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, y);
    }
}
