package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terraingen.util.Point;
import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.Tile;

public class WorldHelper {

    /**
     * Converts a {@link TilePoint} in the world to a {@link Point} on the screen.
     *
     * @param tile the position of the tile as a {@link TilePoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public static Point tileToPixel(@NotNull TilePoint tile) {
        final float x = Tile.WIDTH * tile.x() * 0.75f;
        final float y = -Tile.HEIGHT * (tile.x() / 2.0f + tile.y());
        return new Point((int) x, (int) y);
    }

    /**
     * Converts a {@link Point} on the screen to a {@link TilePoint} in the world. The returned
     * point is the location of the tile that contains the given screen point. It doesn't
     * necessarily exist in the world; it is just the position of a theoretical tile that could
     * exist there. The given point needs to be shifted based on the world center before calling
     * this function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    public static TilePoint pixelToTile(@NotNull Point pos) {
        // Convert it to a fractional tile point
        final float fracX = pos.x() * 2f / 3f / Tile.RADIUS;
        final float fracY = -(pos.x() + (float) Math.sqrt(3) * pos.y()) / (Tile.RADIUS * 3f);
        final float fracZ = -fracX - fracY; // We'll need this later

        // Convert the fraction tile coordinates to regular coordinates
        // First, get rounded versions of each coord
        int roundX = Math.round(fracX);
        int roundY = Math.round(fracY);
        int roundZ = Math.round(fracZ);

        // roundX + roundY + roundZ == 0 is not guaranteed, so we need to recalculate one of them

        // Find how much each one needed to be rounded
        final float xDiff = Math.abs(fracX - roundX);
        final float yDiff = Math.abs(fracY - roundY);
        final float zDiff = Math.abs(fracZ - roundZ);

        // Recalculate the one that rounded the most
        if (xDiff > yDiff && xDiff > zDiff) {
            roundX = -roundY - roundZ;
        } else if (yDiff > zDiff) {
            roundY = -roundX - roundZ;
        } else {
            roundZ = -roundX - roundY;
        }

        return new TilePoint(roundX, roundY, roundZ);
    }
}
