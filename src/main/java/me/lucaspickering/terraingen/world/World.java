package me.lucaspickering.terraingen.world;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.TerrainGen;
import me.lucaspickering.terraingen.render.Renderer;
import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.InclusiveRange;
import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.generate.BeachGenerator;
import me.lucaspickering.terraingen.world.generate.BiomePainter;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.generate.LandRougher;
import me.lucaspickering.terraingen.world.generate.OceanFloorGenerator;
import me.lucaspickering.terraingen.world.generate.PeakGenerator;
import me.lucaspickering.terraingen.world.generate.WaterPainter;
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
    private final Tiles tiles;

    // The pixel location of the getCenter of the world
    private Point worldCenter;

    public World() {
        random = TerrainGen.instance().random();
        tiles = genTiles();
        worldCenter = new Point(Renderer.RES_WIDTH / 2, Renderer.RES_HEIGHT / 2);
    }

    private Tiles genTiles() {
        final Tiles tiles = new Tiles();
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

        // Apply each generator in sequence (this is the heavy lifting)
        Arrays.stream(GENERATORS).forEach(gen -> gen.generate(tiles, random));

        return tiles.immutableCopy(); // Return an immutable copy of what we just made
    }

    /**
     * Gets the world's tiles. No copy is made, but the returned object is immutable. Its
     * internal objects (tiles, etc.) may be mutable though, so do not change them!
     *
     * @return the world's tiles
     */
    public Tiles getTiles() {
        return tiles;
    }

    public Point getWorldCenter() {
        return worldCenter;
    }

    public void setWorldCenter(Point worldCenter) {
        this.worldCenter = worldCenter;
    }
}
