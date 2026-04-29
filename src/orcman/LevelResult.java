package orcman;

/*
 * Author: OrcMan Project
 * Class: LevelResult.java
 *
 * Description:
 *   Enum returned by Level.update() each tick.
 *   Keeps the game-flow logic in GamePanel clean -- no magic integers.
 */

public enum LevelResult {
    RUNNING,       // nothing special happened
    PLAYER_DIED,   // death timer expired; lose a life
    LEVEL_CLEAR    // all dots collected; advance to next level
}
