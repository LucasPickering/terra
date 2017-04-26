package me.lucaspickering.terra.world.step;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import me.lucaspickering.terra.world.Continent;
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
    private static final double WATER_STEP = 0.1;

    private final List<Tile> sortedTiles;

    public FreshWaterStepper(World world, Random random) {
        super(world, random);

        // Sort all land tiles by ascending elevation
        sortedTiles = world.getTiles().stream()
            .filter(t -> t.biome().isLand()) // Filter out water tiles
            .sorted((t1, t2) -> Integer.compare(t2.elevation(), t1.elevation())) // Sort - desc elev
            .collect(Collectors.toList());

        // Init each land tile with some rainwater
        sortedTiles.forEach(t -> t.addWater(RAINFALL)); // Init each tile with some water
    }

    @Override
    public void step() {
        getWorld().getContinents().parallelStream().forEach(this::spreadForContinent);

        // Convert all appropriate tiles to lakes
        // TODO

        // Add rivers based on water traversal patterns
        // TODO
    }

    private void spreadForContinent(Continent continent) {
        final List<Tile> sortedTiles = continent.getTiles().stream()
            .sorted((t1, t2) -> Double.compare(t2.getWaterElevation(),
                                               t1.getWaterElevation())) // Sort by desc water elev
//            .sorted((t1, t2) -> Integer.compare(t2.elevation(),
//                                                t1.elevation())) // Sort by desc elev
            .collect(Collectors.toList());
        for (Tile tile : sortedTiles) {
            equalizeWater(tile);
        }
    }

    private void equalizeWater(Tile tile) {
        final double midWaterLevel = tile.getWaterLevel();
        final double midWaterElev = tile.getWaterElevation();
        final Collection<Tile> adjTiles =
            getWorld().getTiles().getAdjacentTiles(tile.pos()).values();

        // If this tile is adjacent to a water tile, just dump all our water in there
        for (Tile adjTile : adjTiles) {
            if (adjTile.biome().isWater()) {
                tile.clearWater();
                return; // No more water to spread
            }
        }

        // Get all tiles adjacent to this one with a lower water elevation
        final TileSet lowerTiles = adjTiles.stream()
            .filter(adj -> adj.getWaterElevation() < midWaterElev)
            .collect(Collectors.toCollection(TileSet::new));

        final double targetWaterElev = getTargetWaterElev(tile, lowerTiles);

        // If there are any lower tiles to pass water onto, do that
        if (lowerTiles.size() > 0) {
            double totalWaterChanged = 0.0; // TODO remove
            double waterNeeded = 0.0;

            for (Tile workingTile : lowerTiles) {
                final double diff = targetWaterElev - workingTile.getWaterElevation();
                waterNeeded += diff;
            }

            // If we need more water to equalize than is available on this tile, just distribute
            // all the water on this tile evenly and let the other tiles deal with equalization
            if (waterNeeded > midWaterLevel) {
                for (Tile workingTile : lowerTiles) {
                    totalWaterChanged += workingTile.addWater(midWaterLevel / lowerTiles.size());
                }
            } else {
                // Modify the water level on each tile to match the target water elevation
                for (Tile workingTile : lowerTiles) {
                    final double diff = targetWaterElev - workingTile.getWaterElevation();
                    if (diff > 0.0) {
                        totalWaterChanged += workingTile.addWater(diff);
                    }
                    if (diff < 0.0) {
                        totalWaterChanged -= tile.removeWater(-diff);
                    }
                }
            }

            // Remove water from this tile to make it hit the target elevation. If the target is
            // lower than this tile's elevation, just remove all the water
            totalWaterChanged -= tile.removeWater(midWaterElev - targetWaterElev);

            if (Math.abs(totalWaterChanged) > 0.01) {
                System.out.printf("Unaccounted water: [%f] change for [%s]%n",
                                  totalWaterChanged, tile);
            }
        }
    }

    private double getTargetWaterElev(Tile center, TileSet adjTiles) {
        // Calculate the average water elevation across the center tile and its adjacents
        final double totalWaterElev = adjTiles.stream()
                                          .mapToDouble(Tile::getWaterElevation)
                                          .sum() + center.getWaterElevation();
        final double targetWaterElev = totalWaterElev / (adjTiles.size() + 1);

        // Remove any tiles that are too high to get water on them. Move their water onto the
        // center tile.
        boolean changed = false;
        final Iterator<Tile> iter = adjTiles.iterator();
        while (iter.hasNext()) {
            final Tile adjTile = iter.next();
            if (adjTile.elevation() > targetWaterElev) {
                center.addWater(adjTile.getWaterLevel());
                adjTile.clearWater();
                iter.remove();
                changed = true;
            }
        }

        // If we removed any tiles from the set, then try again with fewer tiles. Otherwise,
        // our answer is valid so return it.
        if (changed) {
            return getTargetWaterElev(center, adjTiles);
        }
        return targetWaterElev;
    }
}
