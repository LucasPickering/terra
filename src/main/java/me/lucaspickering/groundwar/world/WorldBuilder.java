package me.lucaspickering.groundwar.world;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import me.lucaspickering.groundwar.util.Pair;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public class WorldBuilder {

    /**
     * Unmodifiable map of {@link Tile.Builder}
     */
    private final Map<TilePoint, Tile.Builder> builders;

    public WorldBuilder(Set<TilePoint> points) {
        // Turn each point into a map entry of point:builder
        final Map<TilePoint, Tile.Builder> builders = points.stream()
            .map(p -> new Pair<>(p, new Tile.Builder(p))) // Create a builder for each point
            .collect(Pair.mapCollector());

        // TODO add adjacents for each tile builder

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

        // TODO add adjacents to each tile

        // Return an unmodifiable map backed by the map we made above
        return Collections.unmodifiableMap(world);
    }
}
