package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldHelper {

    /**
     * Converts a {@link TilePoint} in the world to a {@link Point} on the screen.
     *
     * @param world the world that the tile is in (used to get tile pixel dimensions)
     * @param tile  the position of the tile as a {@link TilePoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public static Point tileToPixel(@NotNull World world, @NotNull TilePoint tile) {
        final double x = world.getTileWidth() * tile.x() * 0.75f;
        final double y = -world.getTileHeight() * (tile.x() / 2.0f + tile.y());
        return world.getWorldCenter().plus((int) x, (int) y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link TilePoint} in the world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there. The given point needs to be shifted based on the world center before calling
     * this function.
     *
     * @param world the world that the tile is in (used to get tile pixel dimensions)
     * @param pos   any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public static TilePoint pixelToTile(@NotNull World world, @NotNull Point pos) {
        final Point shiftedPos = pos.minus(world.getWorldCenter());
        // Convert it to a fractional tile point
        final double fracX = shiftedPos.x() * 4f / 3f / world.getTileWidth();
        final double fracY = -(shiftedPos.x() + Math.sqrt(3) * shiftedPos.y())
                            / (world.getTileRadius() * 3f);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return TilePoint.roundPoint(fracX, fracY, fracZ);
    }

    /**
     * Initializes a collection of {@link Tile}s, in a {@link Tiles} object, of the given size.
     * Each tile will have default biome and elevation, but will have its adjacent tiles properly
     * initialized.
     *
     * @param size the radius of the collection of tiles
     * @return the initialized {@link Tiles}
     */
    @NotNull
    public static Tiles initTiles(int size) {
        final Tiles tiles = new Tiles();
        // Fill out the set with a bunch of points
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                for (int z = -size; z <= size; z++) {
                    if (x + y + z == 0) {
                        tiles.add(new Tile(new TilePoint(x, y, z)));
                    }
                }
            }
        }
        return tiles;
    }
}
