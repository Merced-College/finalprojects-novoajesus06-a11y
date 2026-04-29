package orcman;

/*
 * Author: OrcMan Project
 * Class: Assets.java
 *
 * Description:
 *   Central image loader. Reads every sprite sheet and image from the
 *   /content folder (located at the project root) and slices sprite
 *   sheets into individual frame arrays.
 *
 *   Data structure used: Array (BufferedImage[]) to hold animation frames.
 *   Choosing an array here because the frame count is fixed at load time
 *   and random-access by index is O(1) -- ideal for per-tick animation.
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Assets {

    // ── Sprite frame arrays (arrays of BufferedImage) ────────────────────────
    public static BufferedImage[] soldierWalkFrames;   // 8 frames
    public static BufferedImage[] soldierIdleFrames;   // 6 frames
    public static BufferedImage[] orcWalkFrames;       // 8 frames
    public static BufferedImage[] orcIdleFrames;       // 6 frames

    // ── Single images ────────────────────────────────────────────────────────
    public static BufferedImage background;
    public static BufferedImage swordItem;

    // Frame counts for each sheet
    private static final int WALK_FRAMES = 8;
    private static final int IDLE_FRAMES = 6;

    /*
     * load()
     * Reads every file from the content/ directory and slices sprite sheets.
     * Called once at startup from Main.java.
     *
     * Algorithm:
     *   1. Build file path relative to working directory
     *   2. Read full sheet with ImageIO.read()
     *   3. Slice into frame-width sub-images (sheet width / frame count)
     *   4. Store each frame in a BufferedImage[]
     *
     * Time Complexity: O(F) where F = total frames across all sheets.
     */
    public static void load() {
        soldierWalkFrames = loadSheet("content/soldier_walk.png", WALK_FRAMES);
        soldierIdleFrames = loadSheet("content/soldier_idle.png", IDLE_FRAMES);
        orcWalkFrames     = loadSheet("content/orc_walk.png",     WALK_FRAMES);
        orcIdleFrames     = loadSheet("content/orc_idle.png",     IDLE_FRAMES);
        background        = loadImage("content/background.jpg");
        swordItem         = loadImage("content/sword.png");
    }

    /*
     * loadSheet()
     * Loads a horizontal sprite sheet and slices it into individual frames.
     * Each frame is assumed to be (sheetWidth / frameCount) pixels wide.
     */
    private static BufferedImage[] loadSheet(String path, int frameCount) {
        BufferedImage sheet = loadImage(path);
        if (sheet == null) return new BufferedImage[0];

        int frameW = sheet.getWidth() / frameCount;
        int frameH = sheet.getHeight();

        // Array to store sliced frames -- fixed size, index = frame number
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameW, 0, frameW, frameH);
        }
        return frames;
    }

    /*
     * loadImage()
     * Loads a single image from the given path relative to working directory.
     * Throws RuntimeException on failure so startup fails loudly.
     */
    public static BufferedImage loadImage(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                throw new IOException("Missing file: " + path);
            }
            return ImageIO.read(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + path, e);
        }
    }
}
