package me.lucaspickering.terraingen.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.Pair;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.BiomePainter;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.LandRougher;
import me.lucaspickering.terraingen.world.generate.OceanFloorGenerator;
import me.lucaspickering.terraingen.world.generate.PeakGenerator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

public class World {

    // Every tile's elevation must be in this range
    public static final InclusiveRange ELEVATION_RANGE = new InclusiveRange(-50, 75);

    // World size
    private static final int SIZE = 20;

    private static final Generator[] GENERATORS = new Generator[]{
        new OceanFloorGenerator(),
        new BiomePainter(),
        new LandRougher(),
        new PeakGenerator(),
        new WaterPainter(),
        new BeachGenerator()
    };

    private final Random random;
    private final Map<TilePoint, Tile> tiles;

    // The pixel location of the getCenter of the world
    private Point worldCenter;

    public World() {
        random = TerrainGen.instance().random();
        tiles = genTiles();
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
    }

    private Map<TilePoint, Tile> genTiles() {
        final Map<TilePoint, Tile> tiles = new HashMap<>();
        // Fill out the set with a bunch of points
        for (int x = -SIZE; x <= SIZE; x++) {
            for (int y = -SIZE; y <= SIZE; y++) {
                for (int z = -SIZE; z <= SIZE; z++) {
                    if (x + y + z == 0) {
                        final TilePoint point = new TilePoint(x, y, z);
                        tiles.put(point, new Tile(point));
                    }
                }
            }
        }

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

        // Apply each generator in sequence
        Arrays.stream(GENERATORS).forEach(gen -> gen.generate(tiles, random));

        // Build the world into a map of point:tile and return it
        return buildTiles(tiles);
    }

    private Map<TilePoint, Tile> buildTiles(Map<TilePoint, Tile> tiles) {
        // Turn the map of point:Tile into a map of point:ImmutableTile
        final Map<TilePoint, ? extends Tile> world = tiles.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), new ImmutableTile(e.getValue()))) // Build each tile
            .collect(Pair.mapCollector()); // Collect into a map

        // Return an unmodifiable map backed by the map we just made
        return Collections.unmodifiableMap(world);
    }

    /**
     * Gets the world's copy of tiles. This is NOT a copy, so DO NOT MODIFY IT.
     *
     * @return the world's copy of tiles
     */
    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }

    public Point getWorldCenter() {
        return worldCenter;
    }

    public void setWorldCenter(Point worldCenter) {
        this.worldCenter = worldCenter;
    }
}
