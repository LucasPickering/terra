package me.lucaspickering.terraingen.world;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldBuilder {

    /**
     * Unmodifiable map of {@link Tile.Builder}
     */
    private final Map<TilePoint, Tile.Builder> builders;

    public WorldBuilder(Set<TilePoint> points) {
        // Turn each point into a map entry of point:builder
        final Map<TilePoint, Tile.Builder> builders = points.stream()
            .map(p -> new Pair<>(p, new Tile.Builder(p))) // Create a builder for each point
            .collect(Pair.mapCollector()); // Collect the stream into a map

        for (Map.Entry<TilePoint, Tile.Builder> entry : builders.entrySet()) {
            final TilePoint point = entry.getKey();
            final Tile.Builder builder = entry.getValue();

            // Get all tiles adjacent to this one
            final Map<Direction, Tile.Builder> adjacents = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, TilePoint> adjEntry :
                WorldHelper.getAdjacentTiles(builders.keySet(), point).entrySet()) {
                final Direction dir = adjEntry.getKey();
                final TilePoint adjPoint = adjEntry.getValue();

                // Add the corresponding builder to the map of adjacent tiles
                adjacents.put(dir, builders.get(adjPoint));
            }
            builder.setAdjacents(adjacents);
        }

        this.builders = Collections.unmodifiableMap(builders);
    }

    public Map<TilePoint, Tile.Builder> builders() {
        return builders;
    }

    /**
     * Builds an unmodifiable map of tiles representing the builders.
     *
     * @return the built builders
     */
    public Map<TilePoint, Tile> build() {
        // Turn the map of point:builder into a map of point:tile
        final Map<TilePoint, Tile> world = builders.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue().build())) // Build each tile
            .collect(Pair.mapCollector()); // Collect into a map

        // Now that we have all the built tiles, we can populate each one's adjacents field
        for (Map.Entry<TilePoint, Tile> entry : world.entrySet()) {
            final TilePoint point = entry.getKey();
            final Tile tile = entry.getValue();

            // Get all tiles adjacent to this one
            final Map<Direction, Tile> adjacents = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, TilePoint> adjEntry :
                WorldHelper.getAdjacentTiles(world.keySet(), point).entrySet()) {
                final Direction dir = adjEntry.getKey();
                final TilePoint adjPoint = adjEntry.getValue();

                // Add the corresponding tile to the map of adjacent tiles
                adjacents.put(dir, world.get(adjPoint));
            }
            tile.setAdjacents(adjacents);
        }

        // Return an unmodifiable map backed by the map we made above
        return Collections.unmodifiableMap(world);
    }
}
