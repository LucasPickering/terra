package me.lucaspickering.terra.world.step;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;

/**
 * Simulates rainfall and uses that to generate lakes and rivers. The general approach is as
 * follows:
 * <ul>
 * <li>Drop x liters of water on each land tile</li>
 * <li>For each tile, move its water onto each adjacent tile that is lower than it; apply this
 * to tiles in descending order of elevation</li>
 * <li>If there is no where for a tile's water to go, and its water level hits some threshold,
 * that tile becomes a lake</li>
 * <li>At each step keep track of how much water has moved across each tile; if this hits some
 * threshold, that tile becomes a river</li>
 * </ul>
 */
public class FreshWaterStepper extends Stepper {

    private static final double RAINFALL = 0.5;
    private static final double LAKE_THRESHOLD = 1.0;
    private static final double RIVER_THRESHOLD = 10.0;

    private final List<Tile> sortedTiles;

    public FreshWaterStepper(World world, Random random) {
        super(world, random);

        // Sort all land tiles by ascending elevation
        sortedTiles = world.getTiles().stream()
            .filter(t -> t.biome().isLand()) // Filter out water tiles
            .sorted(Comparator.comparingInt(Tile::elevation)) // Sort by ascending elev
            .collect(Collectors.toList());

        // Init each land tile with some rainwater
        sortedTiles.forEach(t -> t.addWater(RAINFALL)); // Init each tile with some water
    }

    @Override
    public void step() {
        // Trickle the water downhill
        for (Tile tile : sortedTiles) {
            spreadWater(tile);
        }

        // Convert all appropriate tiles to lakes
        // TODO

        // Add rivers based on water traversal patterns
        // TODO
    }

    /**
     * Moves all the water on the given tile to tiles that are adjacent to with a lower water
     * elevation.
     *
     * @param tile the tile to spread water from
     */
    private void spreadWater(Tile tile) {
        final double waterElev = tile.getWaterElevation();
        // Get all tiles adjacent to this one with a lower water elevation
        final TileSet lowerTiles = getWorld().getTiles().getAdjacentTiles(tile.pos())
            .values().stream()
            .filter(adj -> adj.getWaterElevation() < waterElev)
            .collect(Collectors.toCollection(TileSet::new));

        // If there are any lower tiles to pass water onto, do that
        if (lowerTiles.size() > 0) {
            final double totalElevDiff = lowerTiles.stream()
                .mapToDouble(t -> Math.abs(waterElev - t.getWaterElevation()))
                .sum();
            // Amount of water to pass on to each lower tile
            final double waterToSpread = tile.getWaterLevel();
            for (Tile adjTile : lowerTiles) {
                // Runoff is distributed proportional to water elevation difference
                final double runoffPercentage = (waterElev - adjTile.getWaterElevation()) /
                                                totalElevDiff;
                adjTile.addWater(waterToSpread * runoffPercentage);
            }
            tile.clearWater(); // Remove all water from this tile
        }
    }
}
