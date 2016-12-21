package me.lucaspickering.groundwar.world;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public final class WorldBuilder {

    /**
     * Unmodifiable map of {@link Tile.Builder}
     */
    private final Map<TilePoint, Tile.Builder> builders;

    public WorldBuilder(Set<TilePoint> points) {
        // Turn each point into a map entry of point:builder
        final Map<TilePoint, Tile.Builder> builders = points.stream()
            .collect(Collectors.toMap(p -> p, // Key is the point
                                      Tile.Builder::new)); // Value is a tile builder

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
            .collect(Collectors.toMap(Map.Entry::getKey, // Key is still the point
                                      e -> e.getValue().build())); // Value is the built tile

        // TODO add adjacents to each tile

        // Return an unmodifiable map backed by the map we made above
        return Collections.unmodifiableMap(world);
    }
}
