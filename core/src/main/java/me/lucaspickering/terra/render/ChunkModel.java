package me.lucaspickering.terra.render;

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

import java.util.EnumMap;
import java.util.Map;

import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPoint;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.utils.Point2;

public class ChunkModel implements RenderableProvider {

    // Distance from the center of the tile to each VERTEX
    private static final double TILE_RADIUS = 50.0;
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

    // The hexagonal prism model used for all tiles. This will be created once, scaled and colored
    // when creating a ModelInstance from it. Pls no modify!
    public static final Model TILE_MODEL;

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
     * Converts a {@link HexPoint} in to a point in 2D space.
     *
     * @param tilePos the position of the tile as a {@link HexPoint}
     * @return the position of that tile's center on the screen
     */
    @NotNull
    public static Point2 tileToPixel(@NotNull HexPoint tilePos) {
        final double x = TILE_WIDTH * tilePos.x() * 0.75;
        final double y = -TILE_DEPTH * (tilePos.x() / 2.0 + tilePos.y());
        return new Point2(x, y);
    }

    /**
     * Gets the rendered height of the given tile, based on its elevation. All tiles will be of
     * height {@code maxElev - minElev + 1}.
     *
     * If the elevation range is [-1000, 1000], a tile with elevation -1000 will be height 1, and a
     * tile of elevation 1000 will be height 2001.
     *
     * @param tile the tile
     * @return the rendered height of the given tile
     */
    public static double getTileHeight(@NotNull Tile tile) {
        return tile.elevation() - World.ELEVATION_RANGE.lower() + 1.0;
    }

    /**
     * Get the rendered position of the center of the top face of the given tile.
     *
     * @param tile the tile
     * @return the (x,y,z) position of the center of the top of the tile
     */
    public static Vector3 getTilePos(@NotNull Tile tile) {
        // Get (x,y,z) of the top-center of the tile
        final Point2 tilePos = ChunkModel.tileToPixel(tile.pos());
        return new Vector3((float) tilePos.x(),            // x based on position
                           (float) getTileHeight(tile), // y based on elevation
                           (float) tilePos.y());           // z based on position
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
    public static HexPoint pixelToTile(@NotNull Point2 pos) {
        // Convert it to a fractional tile point
        final double fracX = pos.x() * 4.0 / 3.0 / TILE_WIDTH;
        final double fracY = -(pos.x() + Math.sqrt(3.0) * pos.y())
                             / (TILE_WIDTH * 1.5);
        final double fracZ = -fracX - fracY; // We'll need this later

        // Return the rounded point
        return HexPoint.roundPoint(fracX, fracY, fracZ);
    }

    private final HexPointMap<Tile, ModelInstance> tileModelInsts = new HexPointMap<>();
    private final ModelCache tileModelCache = new ModelCache();
    private final Map<TileOverlay, ModelCache> overlayModelCaches =
        new EnumMap<>(TileOverlay.class);

    /**
     * Initialize a model for the given chunk with the given color mode. The color mode can be
     * changed later with {@link #setColorMode}.
     *
     * @param chunk         the chunk to model
     * @param tileColorMode the mode to derive each tile's color
     */
    public ChunkModel(Chunk chunk, TileColorMode tileColorMode) {
        // Populate the overlay cache map with an empty cache for each overlay
        for (TileOverlay overlay : TileOverlay.values()) {
            final ModelCache modelCache = new ModelCache();
            modelCache.begin(); // The corresponding end() WILL happen, I promise
            overlayModelCaches.put(overlay, modelCache);
        }

        chunk.getTiles().forEach(this::initTileModels); // Initialize each tile's models
        setColorMode(tileColorMode); // Init the color for each tile
        buildModelCaches(); // Build the caches based on the models we just made
    }

    /**
     * Create a {@link ModelInstance} for an individual tile and add it to {@link #tileModelInsts}.
     *
     * @param tile the tile to create a model for
     */
    private void initTileModels(Tile tile) {
        // CALCULATE TRANSFORMATIONS
        // Calculate the height of the tile that we want the tile to be drawn with
        final float tileHeight = (float) getTileHeight(tile);

        final Point2 tilePos = tileToPixel(tile.pos());
        final Vector3 translate = new Vector3((float) tilePos.x(),     // Set x based on position
                                              tileHeight / 2f, // Shift up based on height
                                              (float) tilePos.y());    // Set z based on position
        final Quaternion rotate = new Quaternion(); // No rotation
        final Vector3 scale = new Vector3(1f, tileHeight, 1f); // Scale based on height

        // Instantiate the model with the transformations and save it
        tileModelInsts.put(tile, new ModelInstance(TILE_MODEL, new Matrix4(translate,
                                                                           rotate,
                                                                           scale)));

        // Build the necessary models for each overlay, and each one to its respect cache
        for (TileOverlay overlay : TileOverlay.values()) {
            final ModelCache modelCache = overlayModelCaches.get(overlay);
            overlay.addRenderables(tile, modelCache);
        }
    }

    public void setColorMode(TileColorMode tileColorMode) {
        tileModelInsts.forEach((tile, mi) -> {
            // Set the color for the model instance based on the tile
            final Color color = tileColorMode.getColor(tile);
            mi.materials.get(0).set(ColorAttribute.createDiffuse(color));
        });
    }

    private void buildModelCaches() {
        // Add all tile model instances to the main cache
        tileModelCache.begin();
        tileModelCache.add(tileModelInsts.values()); // Add tile models
        tileModelCache.end();

        overlayModelCaches.values().forEach(ModelCache::end); // Finalize the cache for each overlay
    }

    public RenderableProvider getTileModels() {
        return tileModelCache;
    }

    public RenderableProvider getTileOverlayModels(TileOverlay overlay) {
        return overlayModelCaches.get(overlay);
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
