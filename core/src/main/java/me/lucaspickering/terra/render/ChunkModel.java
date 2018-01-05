package me.lucaspickering.terra.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import org.jetbrains.annotations.NotNull;

import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.utils.Point2;

public class ChunkModel implements RenderableProvider {

    // Distance from the center of the tile to each VERTEX
    private static final double TILE_RADIUS = 5.0;
    // Distance between opposite vertices
    private static final double TILE_WIDTH = TILE_RADIUS * 2;
    // Distance between midpoints of opposite sides
    private static final double TILE_DEPTH = Math.sqrt(3) * TILE_RADIUS;
    private static final Point2[] TILE_SIDE_MIDPOINTS = {
        new Point2(0, -TILE_DEPTH / 2),                         // North
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, -TILE_DEPTH / 4), // Northeast
        new Point2(+(3.0 / 8.0) * TILE_WIDTH, +TILE_DEPTH / 4), // Southest
        new Point2(0, TILE_DEPTH / 2),                          // South
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, +TILE_DEPTH / 4), // Southwest
        new Point2(-(3.0 / 8.0) * TILE_WIDTH, -TILE_DEPTH / 4)  // Northwest
    };

    private static final Attribute RUNOFF_COLOR_ATTR = ColorAttribute.createDiffuse(Colors.RUNOFF);
    private static final Attribute WATER_BLENDING_ATTR = new BlendingAttribute(0.15f);

    // The hexagonal prism model used for all tiles. This will be created once, scaled and colored
    // when creating a ModelInstance from it.
    private static final Model TILE_MODEL;

    static {
        // Initialize the tile model
        final ModelBuilder modelBuilder = new ModelBuilder();

        // A cylinder with 6 divisions just happens to be a hexagon
        TILE_MODEL = modelBuilder.createCylinder((float) TILE_WIDTH, 1f, (float) TILE_WIDTH,
                                                 Tile.NUM_SIDES, new Material(),
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

    private final HexPointMap<Tile, ModelInstance> tileModelInsts = new HexPointMap<>();
    private final HexPointMap<Tile, ModelInstance> tileWaterModelInsts = new HexPointMap<>();
    private final ModelCache tileModelCache = new ModelCache();

    /**
     * Initialize a model for the given chunk with the given color mode. The color mode can be
     * changed later with {@link #setColorMode}.
     *
     * @param chunk         the chunk to model
     * @param tileColorMode the mode to derive each tile's color
     */
    public ChunkModel(Chunk chunk, TileColorMode tileColorMode) {
        chunk.getTiles().forEach(this::initTileModelInst); // Create a model for each tile
        setColorMode(tileColorMode); // Init the color for each tile
        buildModelCache(); // Build the cache based on the models we just made
    }

    /**
     * Create a {@link ModelInstance} for an individual tile and add it to {@link #tileModelInsts}.
     *
     * @param tile the tile to create a model for
     */
    private void initTileModelInst(Tile tile) {
        // CALCULATE TRANSFORMATIONS
        // Calculate the height of the tile that we want the tile to be drawn with
        final int tileHeight = tile.elevation() - World.ELEVATION_RANGE.lower() + 1;

        final Point2 tilePos = tileToPixel(tile.pos());
        final Vector3 translate = new Vector3((float) tilePos.x(),  // Translate x based on position
                                              tileHeight / 2f,      // Shift up based on height
                                              (float) tilePos.y()); // Translate x based on position
        final Quaternion rotate = new Quaternion(); // No rotation
        final Vector3 scale = new Vector3(1f, tileHeight, 1f); // Scale based on height

        // Instantiate the model with the transformations and save it
        tileModelInsts.put(tile, new ModelInstance(TILE_MODEL, new Matrix4(translate,
                                                                           rotate,
                                                                           scale)));

        // If this tile has runoff water on it, init a model for the water
        if (tile.getWaterLevel() > 0f) {
            translate.y = tileHeight + (float) tile.getWaterLevel() / 2f;
            scale.y = (float) tile.getWaterLevel();
            final ModelInstance waterModelInst =
                new ModelInstance(TILE_MODEL, new Matrix4(translate, rotate, scale));

            // Add color and transparency material attributes
            waterModelInst.materials.get(0).set(WATER_BLENDING_ATTR);
            waterModelInst.materials.get(0).set(RUNOFF_COLOR_ATTR);

            tileWaterModelInsts.put(tile, waterModelInst); // Save this instance in the map
        }
    }

    public void setColorMode(TileColorMode tileColorMode) {
        tileModelInsts.forEach((tile, mi) -> {
            // Set the color for the model instance based on the tile
            final Color color = tileColorMode.getColor(tile);
            color.a = 0.5f;
            mi.materials.get(0).set(ColorAttribute.createDiffuse(color));
        });
    }

    private void buildModelCache() {
        tileModelCache.begin();
        tileModelInsts.values().forEach(tileModelCache::add); // Add tile models
        tileWaterModelInsts.values().forEach(tileModelCache::add); // Add tile water models
        tileModelCache.end();
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
