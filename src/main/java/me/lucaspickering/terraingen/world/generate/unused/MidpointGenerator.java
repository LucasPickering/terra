package me.lucaspickering.terraingen.world.generate.unused;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.generate.Generator;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.util.TileSet;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.Tile;

public class MidpointGenerator implements Generator {

    private final static float SLOP_FACTOR = 0.2f;

    @Override
    public void generate(World world, Random random) {
        final Map<Direction, Tile> corners = getCorners(world.getTiles());

        // Set the elevation for each corner
        for (Tile tile : corners.values()) {
            final int elev = World.ELEVATION_RANGE.randomIn(random);
            tile.setElevation(elev);
        }

        for (Tile tile1 : corners.values()) {
            for (Tile tile2 : corners.values()) {
                if (!tile1.pos().equals(tile2.pos())) {
                    gradeBetween(world.getTiles(), random, tile1, tile2);
                }
            }
        }

        world.getTiles().forEach(tile -> tile.setBiome(Biome.FOREST));
    }

    /**
     * Gets the corners of a hexagon-shaped set of tiles.
     *
     * @param tiles the tiles to get the corners of
     * @return the corner in each direction
     */
    private Map<Direction, Tile> getCorners(TileSet tiles) {
        final Map<Direction, Tile> result = new EnumMap<>(Direction.class);

        final int radius = 50;
        for (Direction direction : Direction.values()) {
            final TilePoint point = direction.delta().times(radius);
            result.put(direction, tiles.getByPoint(point));
        }

        return result;
    }

    private void gradeBetween(TileSet tiles, Random random, Tile tile1, Tile tile2) {
        // Get the tile between the two givens by averaging the two points in each axis
        final TilePoint midPoint = TilePoint.roundPoint((tile1.pos().x() + tile2.pos().x()) / 2f,
                                                        (tile1.pos().y() + tile2.pos().y()) / 2f,
                                                        (tile1.pos().z() + tile2.pos().z()) / 2f);
        final Tile midTile = tiles.getByPoint(midPoint);
        final int elevDiff = Math.abs(tile1.elevation() - tile2.elevation());
        final int slop = (int) (elevDiff * SLOP_FACTOR) + 1;
        final int avgElev = (tile1.elevation() + tile2.elevation()) / 2;
        midTile.setElevation(Funcs.randomSlop(random, avgElev, slop));

        // If there is space between each tile and the midpoint, recursively smooth between them
        if (!tile1.isAdjacentTo(midTile)) {
            gradeBetween(tiles, random, tile1, midTile);
        }
        if (!tile2.isAdjacentTo(midTile)) {
            gradeBetween(tiles, random, tile2, midTile);
        }
    }
}
