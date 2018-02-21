package me.lucaspickering.terra.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import java.util.*;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.CameraController;
import me.lucaspickering.terra.input.KeyAction;
import me.lucaspickering.terra.render.ChunkModel;
import me.lucaspickering.terra.render.TileColorMode;
import me.lucaspickering.terra.render.TileOverlay;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.WorldHandler;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.utils.GeneralFuncs;

public class WorldScreen extends Screen {

    private static final float FOV = 75f;

    private final Logger logger;
    private final WorldHandler worldHandler;

    private final Camera camera;
    private final CameraController cameraController;
    private final Environment environment;
    private final ModelBatch modelBatch;
    private final HexPointMap<Chunk, ChunkModel> chunkModels = new HexPointMap<>();
    private final Set<TileOverlay> activeTileOverlays = EnumSet.noneOf(TileOverlay.class);

    public WorldScreen(WorldHandler worldHandler) {
        Objects.requireNonNull(worldHandler);

        logger = Logger.getLogger(getClass().getName());
        this.worldHandler = worldHandler;

        initActionHandlers();

        // RENDERING INIT
        // Camera
        camera = new PerspectiveCamera(FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraController = new CameraController(camera);
        initCamera();

        // Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // Model batch
        modelBatch = new ModelBatch();

        initChunkModels();
    }

    private void initActionHandlers() {
        registerKeyAction(KeyAction.WORLD_TILECOLOR_COMPOSITE,
                          () -> setTileColorMode(TileColorMode.COMPOSITE));
        registerKeyAction(KeyAction.WORLD_TILECOLOR_BIOME,
                          () -> setTileColorMode(TileColorMode.BIOME));
        registerKeyAction(KeyAction.WORLD_TILECOLOR_ELEVATION,
                          () -> setTileColorMode(TileColorMode.ELEVATION));
        registerKeyAction(KeyAction.WORLD_TILECOLOR_HUMIDITY,
                          () -> setTileColorMode(TileColorMode.HUMIDITY));
        registerKeyAction(KeyAction.WORLD_TILECOLOR_RUNOFFLEVEL,
                          () -> setTileColorMode(TileColorMode.RUNOFF_LEVEL));

        registerKeyAction(KeyAction.WORLD_TILEOVERLAY_RUNOFFLEVEL,
                          () -> toggleTileOverlay(TileOverlay.RUNOFF_LEVEL));
        registerKeyAction(KeyAction.WORLD_TILEOVERLAY_RUNOFFEXITS,
                          () -> toggleTileOverlay(TileOverlay.RUNOFF_EXITS));
        registerKeyAction(KeyAction.WORLD_TILEOVERLAY_RUNOFFTERMINALS,
                          () -> toggleTileOverlay(TileOverlay.RUNOFF_TERMINALS));
    }

    private void initCamera() {
        camera.position.set(0f, (float) (World.ELEVATION_RANGE.upper() * 2.0), 0f);
        camera.rotate(45, -1f, 0f, 0f);
        camera.near = 1f;
        camera.far = 100000f;
        camera.update();

        Gdx.input.setInputProcessor(cameraController);
    }

    private void initChunkModels() {
        final long time = GeneralFuncs.timed(() -> worldHandler.getWorld().getChunks()
            .forEach(c -> chunkModels.put(c, new ChunkModel(c, TileColorMode.COMPOSITE))));
        logger.info(String.format("Initializing world models took %d ms", time));
    }

    @Override
    public void draw() {
        cameraController.update();

        final Tile tileUnderMouse = getTileUnderMouse();
        if (tileUnderMouse != null) {
            System.out.println(tileUnderMouse.pos());
        }

        // Build a list of everything to render
        final List<RenderableProvider> toRender = new LinkedList<>();

        // Add renderables for each chunk
        for (ChunkModel chunkModel : chunkModels.values()) {
            toRender.add(chunkModel.getTileModels()); // Add the tiles

            // Add models for each overlay that is active
            for (TileOverlay overlay : activeTileOverlays) {
                toRender.add(chunkModel.getTileOverlayModels(overlay));
            }
        }

        // Render each chunk
        modelBatch.begin(camera);
        modelBatch.render(toRender, environment);
        modelBatch.end();
    }

    /**
     * Get the tile under the mouse cursor. This generates a ray originating from the mouse cursor,
     * and checks each tile to see if it intersects the ray. If multiple tiles intersect the ray,
     * the nearest one is returned.
     *
     * @return the tile under the mouse cursor
     */
    private Tile getTileUnderMouse() {
        final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

        // Will store the intersection point for each tile that the ray hits
        // Used to find the nearest tile when multiple are intersected
        final Vector3 intersectPoint = new Vector3();

        Tile nearestTile = null;
        float distToNearestTile = Float.MAX_VALUE;

        // Check each tile for intersection
        for (ChunkModel chunkModel : chunkModels.values()) {
            for (Map.Entry<Tile, BoundingBox> entry : chunkModel.tileBoundingBoxes.entrySet()) {
                final Tile tile = entry.getKey();
                final BoundingBox boundingBox = entry.getValue();

                // Check if the ray intersects this tile
                if (Intersector.intersectRayBounds(ray, boundingBox, intersectPoint)) {
                    // Get the distance between the ray origin and the tile
                    final float distance = ray.origin.dst(intersectPoint);

                    // If this is the closest tile found so far, save it
                    if (distance < distToNearestTile) {
                        nearestTile = tile;
                        distToNearestTile = distance;
                    }
                }
            }
        }

        return nearestTile;
    }

    private void setTileColorMode(TileColorMode tileColorMode) {
        // Set the color mode for each chunk
        final long time = GeneralFuncs.timed(() -> chunkModels.values()
            .forEach(cm -> cm.setColorMode(tileColorMode)));
        logger.finer(String.format("Color update took %d ms", time));
    }

    private void toggleTileOverlay(TileOverlay overlay) {
        if (activeTileOverlays.contains(overlay)) {
            activeTileOverlays.remove(overlay);
        } else {
            activeTileOverlays.add(overlay);
        }
    }

    @Override
    public void dispose() {
        chunkModels.values().forEach(ChunkModel::dispose);
    }

    @Override
    public boolean keyDown(KeyAction action) {
        // Check for any registered actions
        if (super.keyDown(action)) {
            return true;
        }

        // Forward everything else to the camera controller
        return cameraController.keyDown(action);
    }

    @Override
    public boolean keyUp(KeyAction action) {
        // Forward everything else to the camera controller
        return cameraController.keyUp(action);
    }
}
