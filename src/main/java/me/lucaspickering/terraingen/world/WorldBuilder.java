package me.lucaspickering.terraingen.world;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldBuilder {

    /**
     * Unmodifiable map of {@link Tile}s
     */
    private final Map<TilePoint, Tile> tiles;

    public WorldBuilder(Set<TilePoint> points) {
        // Turn each point into a map entry of point:tile
        final Map<TilePoint, Tile> tiles = points.stream()
            .map(p -> new Pair<>(p, new Tile(p))) // Create a tile for each point
            .collect(Pair.mapCollector()); // Collect the stream into a map

        // Add adjacents for each tile
        for (Map.Entry<TilePoint, Tile> entry : tiles.entrySet()) {
            final TilePoint point = entry.getKey();
            final Tile tile = entry.getValue();

            // Get all tiles adjacent to this one
            final Map<Direction, Tile> adjacents = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, TilePoint> adjEntry :
                WorldHelper.getAdjacentTiles(tiles.keySet(), point).entrySet()) {
                final Direction dir = adjEntry.getKey();
                final TilePoint adjPoint = adjEntry.getValue();

                // Add the corresponding tile to the map of adjacent tiles
                adjacents.put(dir, tiles.get(adjPoint));
            }
            tile.setAdjacents(adjacents);
        }

        this.tiles = Collections.unmodifiableMap(tiles);
    }

    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }

    /**
     * Builds an unmodifiable map of tiles representing the tiles.
     *
     * @return the built tiles
     */
    public Map<TilePoint, Tile> build() {
        // Turn the map of point:Tile into a map of point:ImmutableTile
        final Map<TilePoint, ? extends Tile> world = tiles.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), new ImmutableTile(e.getValue()))) // Build each tile
            .collect(Pair.mapCollector()); // Collect into a map

        // Return an unmodifiable map backed by the map we just made
        return Collections.unmodifiableMap(world);
    }
}
