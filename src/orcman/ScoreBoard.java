package orcman;

/*
 * Author: OrcMan Project
 * Class: ScoreBoard.java
 *
 * Description:
 *   Keeps the top-5 high scores for the session using a LinkedList<Integer>
 *   that is kept in descending sorted order via insertion sort.
 *
 *   Data structure: LinkedList<Integer>
 *   Chosen to demonstrate a linked list (SLO A) and because the list is
 *   small (max 5 entries), so the O(n) insertion cost is negligible.
 *
 *   Algorithm: Insertion sort (maintained on every add).
 *   Time Complexity: O(n) per insert where n ≤ 5 → effectively O(1).
 */

import java.util.LinkedList;
import java.util.ListIterator;

public class ScoreBoard {

    private static final int MAX_ENTRIES = 5;

    /*
     * Data structure: LinkedList<Integer> (sorted descending).
     * LinkedList gives O(1) add/remove at a known iterator position,
     * which suits the insertion-sort pattern used below.
     */
    private LinkedList<Integer> scores;

    public ScoreBoard() {
        scores = new LinkedList<>();
    }

    /*
     * add()
     * Inserts a new score into the sorted list (descending) and trims
     * the list to MAX_ENTRIES.
     *
     * Algorithm: Insertion sort -- walk the list until we find the first
     * entry smaller than the new score, then insert before it.
     *
     * Time Complexity: O(n) where n ≤ MAX_ENTRIES = O(1) in practice.
     */
    public void add(int newScore) {
        ListIterator<Integer> it = scores.listIterator();
        boolean inserted = false;

        while (it.hasNext()) {
            int existing = it.next();
            if (newScore >= existing) {
                it.previous();          // back up one position
                it.add(newScore);       // insert before the smaller value
                inserted = true;
                break;
            }
        }

        if (!inserted) scores.addLast(newScore);

        // Trim to max size
        while (scores.size() > MAX_ENTRIES) scores.removeLast();
    }

    /*
     * getHighScore()
     * Returns the highest score recorded this session, or 0 if empty.
     * O(1) -- LinkedList.getFirst() is O(1).
     */
    public int getHighScore() {
        return scores.isEmpty() ? 0 : scores.getFirst();
    }

    /*
     * getTop()
     * Returns a copy of the scores list for display purposes.
     * O(n).
     */
    public LinkedList<Integer> getTop() {
        return new LinkedList<>(scores);
    }

    public boolean isEmpty() { return scores.isEmpty(); }
}
