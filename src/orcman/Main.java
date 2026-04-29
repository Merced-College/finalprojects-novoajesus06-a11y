package orcman;

/*
 * Author: OrcMan Project
 * Class: Main.java
 *
 * Description:
 *   Application entry point.
 *   Loads all assets from the /content folder, then creates the JFrame
 *   containing the GamePanel.
 *
 *   Run from the project root (the folder that contains /content and /src)
 *   so that relative file paths in Assets.java resolve correctly.
 *
 *   In Eclipse:
 *     Run → Run Configurations → Arguments tab
 *     Working directory → "Other" → ${project_loc}
 *   (Eclipse usually sets this automatically.)
 */

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Load images from content/ before any UI is created
        Assets.load();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("OrcMan");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);   // centre on screen
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
