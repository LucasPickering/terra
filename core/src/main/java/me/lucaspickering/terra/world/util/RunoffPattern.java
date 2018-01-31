package me.lucaspickering.terra.world.util;

import java.util.Collections;
import java.util.Map;

import me.lucaspickering.terra.world.Tile;

/**
 * A runoff pattern for a tile has two components: the traversal pattern, and the terminal pattern.
 * The tile associated with a runoff pattern is called the source tile.
 *
 * The traversal pattern for a tile holds information about every step that runoff takes after
 * leaving the source. If you put 1.0 water on the source and start runoff, the traversal pattern
 * tells you exactly which tiles that water will run over, and exactly how much water will go over
 * each tile.
 *
 * A terminal is a tile with no exits. The terminal pattern shows where runoff from the source will
 * end up. It is a map of tile:double, where each key is a terminal tile and the double is a
 * fraction [0, 1] denoting how much of the source's runoff should end up on that terminal. The
 * values in the map should sum to 1, unless. The difference between their sum and 1 is the portion
 * of runoff that gets expelled to the ocean. If a tile has no terminals, then all of its runoff
 * ends up in the ocean and it is dubbed a "sink".
 *
 * A runoff pattern does not hold any state, meaning it does not maintain any information about how
 * much water any tile holds, how much water has traversed the tile, etc. The pattern only maintains
 * a static pattern of how water should move from/through the source.
 */
public class RunoffPattern {

    private final Tile source;
    private final Map<Tile, Double> terminals = new HexPointMap<>();
    private final Map<Tile, Double> exits = new HexPointMap<>();

    public RunoffPattern(Tile source) {
        this.source = source;
    }

    /**
     * Add the given tile as an exit to this runoff pattern. The given exit tile must be adjacent to
     * the source tile of this pattern.
     *
     * @param tile   the tile that runoff will exit through
     * @param factor the portion of runoff from the source that will exit through this tile [0, 1]
     */
    public void addExit(Tile tile, double factor) {
        exits.put(tile, factor); // Add the exit to the traversal pattern

        final RunoffPattern otherRunoffPattern = tile.getRunoffPattern();

        if (otherRunoffPattern.isTerminal()) {
            // If this exit is a terminal, add it as a terminal to us
            terminals.put(tile, factor);
        } else {
            // Otherwise, add all of the tile's terminals to our terminal pattern, but scale their
            // factors by this exit's factor
            otherRunoffPattern.terminals.forEach((t, f) -> terminals.put(t, factor * f));
        }
    }

    /**
     * Determine if this tile is a terminal. A terminal has no exits.
     *
     * @return {@code true} is this tile is a terminal, {@code false} otherwise
     */
    public boolean isTerminal() {
        return exits.isEmpty();
    }

    public Map<Tile, Double> getTerminals() {
        return Collections.unmodifiableMap(terminals);
    }

    public Map<Tile, Double> getExits() {
        return Collections.unmodifiableMap(exits); // Immutability!
    }

    public void distributeRunoff() {
        final double toDistribute = source.clearRunoff(); // Remove all water from this tile
//        addTraversedRunoff(toDistribute, false);
        terminals.forEach((tile, factor) -> tile.addRunoff(toDistribute * factor));
    }

    private void addTraversedRunoff(double traversed, boolean addToSelf) {
        if (addToSelf) {
//            source.addRunoffTraversed(traversed);
        }

        // Traverse the appropriate amount of water over each exit - that will cascade through the
        // network
        exits.forEach((tile, factor) -> tile.getRunoffPattern()
            .addTraversedRunoff(traversed * factor, true));
    }
}
