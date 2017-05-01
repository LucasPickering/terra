package me.lucaspickering.terra.world.generate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.Biome;
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
public class RunoffGenerator extends Generator {

    private static final double RAINFALL = 0.5;
    private static final double LAKE_THRESHOLD = 3.0;
    private static final double RIVER_THRESHOLD = 10.0;
    private static final int ITERATIONS = 5;
    private static final double TOLERABLE_CHANGE_THRESHOLD = 0.001;

    public RunoffGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        final List<Continent> continents = world().getContinents();
        initWaterLevels(world());
        for (int i = 0; i < ITERATIONS; i++) {
            continents.parallelStream().forEach(this::spreadForContinent);
        }
        continents.parallelStream().forEach(this::generateLakes);
        continents.parallelStream().forEach(this::generateRivers);
    }

    /**
     * Initializes each land to a default water level.
     */
    private void initWaterLevels(World world) {
        for (Continent continent : world.getContinents()) {
            for (Tile tile : continent.getTiles()) {
                tile.addWater(RAINFALL);
            }
        }
    }

    private void generateLakes(Continent continent) {
        for (Tile tile : continent.getTiles()) {
            if (tile.getWaterLevel() >= LAKE_THRESHOLD) {
                tile.setBiome(Biome.LAKE);
            }
        }
    }

    private void generateRivers(Continent continent) {
        final TileSet riverTiles = continent.getTiles().parallelStream()
            .filter(t -> t.getWaterTraversed() >= RIVER_THRESHOLD)
            .collect(Collectors.toCollection(TileSet::new));
        final List<Tile> sortedRiverTiles = riverTiles.stream()
            .sorted((t1, t2) -> Integer.compare(t2.elevation(), t1.elevation()))
            .collect(Collectors.toList());

        for (Tile tile : sortedRiverTiles) {
            for (Direction dir : Direction.values()) {
                tile.addRiverConnection(dir, Tile.RiverConnection.ENTRY);
            }
        }
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
        final Collection<Tile> adjTiles = world().getTiles().getAdjacentTiles(tile.pos()).values();

        // If this tile is adjacent to a water tile, just dump all our water in there
        for (Tile adjTile : adjTiles) {
            if (adjTile.biome().isWater()) {
                tile.clearWater();
                return; // No more water to spread
            }
        }

        // Get all tiles adjacent to this one with a lower water elevation
        final TileSet lowerTiles = adjTiles.stream()
            .filter(adj -> adj.getWaterElevation() < tile.getWaterElevation())
            .collect(Collectors.toCollection(TileSet::new));

        final double targetWaterElev = getTargetWaterElev(tile, lowerTiles);

        logStatus(tile, lowerTiles, targetWaterElev); // Log for debugging

        // If there are any lower tiles to pass water onto, do that
        if (lowerTiles.size() > 0) {
            setWaterLevels(tile, lowerTiles, targetWaterElev);
        }
    }

    private double getTargetWaterElev(Tile tile, TileSet adjTiles) {
        // Calculate the average water elevation across the center tile and its adjacents
        final double totalWaterElev = adjTiles.stream()
                                          .mapToDouble(Tile::getWaterElevation)
                                          .sum() + tile.getWaterElevation();
        final double targetWaterElev = totalWaterElev / (adjTiles.size() + 1);

        // Remove any tiles that are too high to get water on them. Move their water onto the
        // center tile.
        boolean changed = false;
        final Iterator<Tile> iter = adjTiles.iterator();
        while (iter.hasNext()) {
            final Tile adjTile = iter.next();
            if (adjTile.elevation() > targetWaterElev) {
                tile.addWater(adjTile.getWaterLevel());
                adjTile.clearWater();
                iter.remove();
                changed = true;
            }
        }

        // If we removed any tiles from the set, then try again with fewer tiles. Otherwise,
        // our answer is valid so return it.
        if (changed) {
            return getTargetWaterElev(tile, adjTiles);
        }
        return targetWaterElev;
    }

    private void setWaterLevels(Tile tile, TileSet adjTiles, double targetWaterElev) {
        double totalWaterChanged = 0.0; // Used to ensure that no water gets added or removed

        // Water needed to equalize across all adjacent tiles
        final double waterNeeded = adjTiles.stream()
            .mapToDouble(t -> targetWaterElev - t.getWaterElevation())
            .sum();

        // If we need more water to equalize than is available on this tile, just distribute
        // all the water on this tile evenly and let the other tiles deal with equalization
        if (waterNeeded > tile.getWaterLevel()) {
            for (Tile adjTile : adjTiles) {
                totalWaterChanged += adjTile.addWater(tile.getWaterLevel() / adjTiles.size());
            }
        } else {
            // Modify the water level on each tile to match the target water elevation
            for (Tile adjTile : adjTiles) {
                final double diff = targetWaterElev - adjTile.getWaterElevation();
                if (diff > 0.0) {
                    totalWaterChanged += adjTile.addWater(diff);
                } else if (diff < 0.0) {
                    totalWaterChanged -= adjTile.removeWater(-diff);
                }
            }
        }

        // Remove water from this tile to make it hit the target elevation
        totalWaterChanged -= tile.removeWater(tile.getWaterElevation() - targetWaterElev);

        // Ensure that no water entered or left the system
        if (Math.abs(totalWaterChanged) > TOLERABLE_CHANGE_THRESHOLD) {
            throw new IllegalStateException(String.format("Intolerable water change [%f] for [%s]",
                                                          totalWaterChanged, tile));
        }
    }

    private void logStatus(Tile tile, TileSet adjTiles, double targetWaterElev) {
        // Log the center tile
        logger().log(Level.FINEST, String.format(
            "Center tile: [%s]  Elevation: [%d]  Water Level: [%f]  Water Elev: [%f]",
            tile.pos(), tile.elevation(), tile.getWaterLevel(), tile.getWaterElevation()));

        // Log the target water level that was calculated
        logger().log(Level.FINEST, String.format("  Calculated target water level: [%f]",
                                                 targetWaterElev));

        // Log each adjacent tile
        for (Tile adjTile : adjTiles) {
            logger().log(Level.FINEST, String.format(
                "  Adj. tile: [%s]  Elevation: [%d]  Water Level: [%f]  Water Elev: [%f]",
                adjTile.pos(), adjTile.elevation(), adjTile.getWaterLevel(),
                adjTile.getWaterElevation()));
        }
    }
}
