package me.lucaspickering.terra.world.generate;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.RunoffPattern;
import me.lucaspickering.utils.Pair;

/**
 * Simulates rainfall, which will later be used to determine where to generate lakes and rivers.
 */
public class RunoffGenerator extends Generator {

    private static final double RAINFALL = 10.0;

    public RunoffGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        final List<Continent> continents = world().getContinents();
        initWaterLevels(world());
        continents.parallelStream().forEach(this::doContinentRunoff);
    }

    /**
     * Initializes each land to a default water level.
     */
    private void initWaterLevels(World world) {
        for (Continent continent : world.getContinents()) {
            for (Tile tile : continent.getTiles()) {
                tile.addRunoff(RAINFALL);
            }
        }
    }

    /**
     * Simulates water runoff for the given continent. This assumes that the continent has already
     * been populated with water.
     *
     * @param continent the continent
     */
    private void doContinentRunoff(Continent continent) {
        // Sort the tiles
        final List<Tile> sortedTiles = continent.getTiles().stream()
            .sorted(Comparator.comparingInt(Tile::elevation)) // Sort by ascending elevation
            .collect(Collectors.toList());

        // Starting at the lowest tile, initialize the runoff pattern for each tile. This has to
        // start at the lowerst tile so that higher tiles can reference lowers tiles
        sortedTiles.forEach(this::initRunoffPattern);

        // Apply the runoff pattern for each tile to move all the water
        sortedTiles.forEach(t -> t.getRunoffPattern().distributeRunoff());
    }

    private void initRunoffPattern(Tile tile) {
        final Collection<Tile> adjTiles = world().getTiles().getAdjacentTiles(tile.pos()).values();
        final RunoffPattern runoffPattern = tile.getRunoffPattern();

        // If any of the adjacent tiles are water, just dump all our runoff in there
        final List<Tile> adjWaterTiles = adjTiles.stream()
            .filter(t -> t.biome().isWater())
            .collect(Collectors.toList());
        if (!adjWaterTiles.isEmpty()) {
            // Add each exit to the traversal pattern - evenly distribute the runoff among them
            final double factor = 1.0 / adjWaterTiles.size(); // Amount of runoff for each tile
            adjWaterTiles.forEach(t -> runoffPattern.addExit(t, factor));
        } else {
            // Add each lower adjacent tile as a runoff exit for this one. The runoff will be
            // divided among the tiles according to a "share" system:
            //   - Each tile gets 1 share by default
            //   - Each tile gets 1 additional share for each meter of elevation diff from this tile
            // The percentage of water that a tile gets is its number of shares divided by the total

            // Get all tiles adjacent to this one with lower elevation, along with the number
            // of shares for each tile
            final List<Pair<Tile, Integer>> lowerTiles = adjTiles.stream()
                .filter(adj -> adj.elevation() < tile.elevation())
                .map(t -> new Pair<>(t, tile.elevation() - t.elevation() + 1)) // Calc # of shares
                .collect(Collectors.toList());

            final int totalShares = lowerTiles.stream()
                .mapToInt(Pair::second) // Get number of shares from each pair
                .sum(); // Sum all the shares

            for (Pair<Tile, Integer> pair : lowerTiles) {
                final Tile lowerTile = pair.first();
                final int shares = pair.second();
                // Add the adjacent tile as a runoff exit, with the appropriate number of shares
                runoffPattern.addExit(lowerTile, (double) shares / totalShares);
            }
        }
    }
}
