package me.lucaspickering.terra.render.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.utils.Point2;

public class ChunkModel implements RenderableProvider {

    // Distance from the center of the tile to each VERTEX
    private static final double TILE_RADIUS = 5.0;
    // Distance between opposite vertices
    private static final double TILE_WIDTH = TILE_RADIUS * 2;
    // Distance between midpoints of opposite sides
    private static final double TILE_DEPTH = Math.sqrt(3) * TILE_RADIUS;
    private static final Point2[] TILE_VERTICES = {
        new Point2(-TILE_WIDTH / 4, -TILE_DEPTH / 2),  // Top-left
        new Point2(+TILE_WIDTH / 4, -TILE_DEPTH / 2),  // Top-right
        new Point2(+TILE_WIDTH / 2, 0),                // Right
        new Point2(+TILE_WIDTH / 4, +TILE_DEPTH / 2),  // Bottom-right
        new Point2(-TILE_WIDTH / 4, +TILE_DEPTH / 2),  // Bottom-left
        new Point2(-TILE_WIDTH / 2, 0)                 // Left
    };
    private static final Point2[] TILE_SIDE_MIDPOINTS = {
        new Point2(0, -TILE_DEPTH / 2),                         // North
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, -TILE_DEPTH / 4), // Northeast
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, +TILE_DEPTH / 4), // Southest
        new Point2(0, TILE_DEPTH / 2),                          // South
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, +TILE_DEPTH / 4), // Southwest
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, -TILE_DEPTH / 4)  // Northwest
    };

    private static final Model TILE_MODEL;

    static {
        // Initialize the tile model
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material();

        // A cylinder with 6 divisions just happens to be a hexagon
        TILE_MODEL = modelBuilder.createCylinder((float) TILE_WIDTH, 1f, (float) TILE_WIDTH,
                                                 Tile.NUM_SIDES, material,
                                                 VertexAttributes.Usage.Position
                                                 | VertexAttributes.Usage.Normal);
    }

    /**
     * Converts a {@link HexPoint} in to a point on the screen.
     *
     * @param tilePos the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    private static Point2 tileToPixel(@NotNull HexPoint tilePos) {
        final double x = TILE_WIDTH * tilePos.x() * 0.75;
        final double y = -TILE_DEPTH * (tilePos.x() / 2.0 + tilePos.y());
        return new Point2(x, y);
    }

    /**
     * Converts a point on the screen to a {@link HexPoint}. The returned point is the location of
     * the tile that contains the given screen point. It doesn't necessarily exist in the world; it
     * is just the position of a theoretical tile that could exist there. The given point needs to
     * be shifted and scaled to be in the proper coordinate system before being passed to this
     * function.
     *
     * @param pos any point on the screen
     * @return the position of the tile that encloses the given point
     */
    @NotNull
    private static HexPoint pixelToTile(@NotNull Point2 pos) {
        // Convert it to a fractional tile point
        final double fracX = pos.x() * 4.0 / 3.0 / TILE_WIDTH;
        final double fracY = -(pos.x() + Math.sqrt(3.0) * pos.y())
                             / (TILE_WIDTH * 1.5);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return HexPoint.roundPoint(fracX, fracY, fracZ);
    }

    /**
     * Gets the pixel position of the center of the tile at the bottom-left of the given chunk.
     *
     * @param chunkPos the chunk
     * @return the center of the tile at the bottom-left of the chunk
     */
    @NotNull
    private static Point2 chunkToPixel(@NotNull HexPoint chunkPos) {
        return tileToPixel(Chunk.getChunkOrigin(chunkPos));
    }

    private final ModelCache tileModelCache = new ModelCache();


    public ChunkModel(Chunk chunk) {
        // Create a model for each tile and store it in the cache
        tileModelCache.begin();
        chunk.getTiles().forEach(t -> tileModelCache.add(createModelInstance(t)));
        tileModelCache.end();
    }

    /**
     * Create a {@link ModelInstance} for an individual tile. The model will be colored, scaled, and
     * translated according to the tile's properties.
     *
     * @param tile the tile to create a model for
     * @return the {@link ModelInstance}
     */
    private ModelInstance createModelInstance(Tile tile) {
        // CALCULATE TRANSFORMATIONS
        // Calculate the height of the tile that we want the tile to be drawn with
        final int tileHeight = tile.elevation() - World.ELEVATION_RANGE.lower() + 1;

        final Point2 tilePos = tileToPixel(tile.pos());
        final Matrix4 transform = new Matrix4(
            new Vector3((float) tilePos.x(),     // Translate x/z based on position
                        tileHeight / 2f,         // Shift up based on height
                        (float) tilePos.y()),
            new Quaternion(),                    // No rotation
            new Vector3(1f, tileHeight, 1f)      // Scale based on height
        );

        // Instantiate the model with the transformations
        final ModelInstance modelInstance = new ModelInstance(TILE_MODEL, transform);

        // Create a material for this tile and add it to the model instance
        modelInstance.materials.get(0).set(createTileMaterial(tile));

        return modelInstance; // Job's done
    }

    private Material createTileMaterial(Tile tile) {
        final Color color = TileColorMode.COMPOSITE.getColor(tile);
        return new Material(ColorAttribute.createDiffuse(color));
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        // Add all models from the cache to the array of renderables
        tileModelCache.getRenderables(renderables, pool);
    }

    public void dispose() {
        tileModelCache.dispose();
    }
}
