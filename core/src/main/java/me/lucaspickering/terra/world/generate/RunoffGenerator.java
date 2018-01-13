package me.lucaspickering.terra.world.generate;

import java.util.*;
import java.util.stream.Collectors;

import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;

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
        continents.parallelStream().forEach(this::spreadForContinent);
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

    private void spreadForContinent(Continent continent) {
        final List<Tile> sortedTiles = continent.getTiles().stream()
            .sorted(Comparator.comparingInt(Tile::elevation).reversed()) // Sort by desc elev
            .collect(Collectors.toList());
        for (Tile tile : sortedTiles) {
            pushWater(tile);
        }
    }

    private void pushWater(Tile tile) {
        final Collection<Tile> adjTiles = world().getTiles().getAdjacentTiles(tile.pos()).values();

        // If any of the adjacent tiles are water, just dump all our runoff in there
        if (adjTiles.stream().anyMatch(t -> t.biome().isWater())) {
            tile.clearWater();
            return; // No more water to spread
        }

        // Get all tiles adjacent to this one with a lower water elevation
        final TileSet lowerTiles = adjTiles.stream()
            .filter(adj -> adj.elevation() < tile.elevation())
            .collect(Collectors.toCollection(TileSet::new));

        if (!lowerTiles.isEmpty()) {
            final double waterToPush = tile.clearWater();
            final double totalElevDiff = lowerTiles.stream()
                .mapToInt(t -> tile.elevation() - t.elevation())
                .sum();

            double totalPushedWater = 0.0;
            for (Tile lowerTile : lowerTiles) {
                final double toAdd = (tile.elevation() - lowerTile.elevation()) / totalElevDiff
                                     * waterToPush;
                totalPushedWater += lowerTile.addWater(toAdd);
            }
            assert Math.abs(totalPushedWater - waterToPush) < 0.001;
        }
    }

    private void logStatus(Tile tile, TileSet adjTiles, double targetWaterElev) {
        // Log the center tile
        logger().finest(String.format(
            "Center tile: [%s]  Elevation: [%d]  Water Level: [%f]  Water Elev: [%f]",
            tile.pos(), tile.elevation(), tile.getWaterLevel(), tile.getWaterElevation()));

        // Log the target water level that was calculated
        logger().finest(String.format("  Calculated target water level: [%f]", targetWaterElev));

        // Log each adjacent tile
        for (Tile adjTile : adjTiles) {
            logger().finest(String.format(
                "  Adj. tile: [%s]  Elevation: [%d]  Water Level: [%f]  Water Elev: [%f]",
                adjTile.pos(), adjTile.elevation(), adjTile.getWaterLevel(),
                adjTile.getWaterElevation()));
        }
    }
}
