package me.lucaspickering.terra.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import java.util.Objects;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.CameraController;
import me.lucaspickering.terra.render.ChunkModel;
import me.lucaspickering.terra.util.Funcs;
import me.lucaspickering.terra.world.TileColorMode;
import me.lucaspickering.terra.world.WorldHandler;
import me.lucaspickering.terra.world.util.Chunk;
import me.lucaspickering.terra.world.util.HexPointMap;
import me.lucaspickering.utils.Point2;

public class WorldScreen extends Screen {

    private static final float FOV = 75f;

    private final Logger logger;
    private final WorldHandler worldHandler;

    private final Camera camera;
    private final CameraController cameraController;
    private final Environment environment;
    private final ModelBatch modelBatch;
    private final HexPointMap<Chunk, ChunkModel> chunkModels = new HexPointMap<>();

    public WorldScreen(WorldHandler worldHandler) {
        Objects.requireNonNull(worldHandler);

        logger = Logger.getLogger(getClass().getName());
        this.worldHandler = worldHandler;

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

    private void initCamera() {
        camera.position.set(0f, 400f, 0f);
        camera.rotate(45, -1f, 0f, 0f);
        camera.near = 1f;
        camera.far = 3000f;
        camera.update();

        Gdx.input.setInputProcessor(cameraController);
    }

    private void initChunkModels() {
        final long time = Funcs.timed(() -> worldHandler.getWorld().getChunks()
            .forEach(c -> chunkModels.put(c, new ChunkModel(c, TileColorMode.COMPOSITE))));
        logger.info(String.format("Initializing world models took %d ms", time));
    }

    @Override
    public void draw(Point2 mousePos) {
        cameraController.update();

        // Render each chunk
        modelBatch.begin(camera);
        modelBatch.render(chunkModels.values(), environment);
        modelBatch.end();
    }

    private void regenerateWorld() {
        worldHandler.generate();
    }

    private void setTileColorMode(TileColorMode tileColorMode) {
        // Set the color mode for each chunk
        final long time = Funcs.timed(() -> chunkModels.values()
            .forEach(cm -> cm.setColorMode(tileColorMode)));
        logger.finer(String.format("Color update took %d ms", time));
    }

    @Override
    public void dispose() {
        chunkModels.values().forEach(ChunkModel::dispose);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.NUM_1:
                setTileColorMode(TileColorMode.COMPOSITE);
                return true;
            case Input.Keys.NUM_2:
                setTileColorMode(TileColorMode.ELEVATION);
                return true;
            case Input.Keys.NUM_3:
                setTileColorMode(TileColorMode.HUMIDITY);
                return true;
            case Input.Keys.NUM_4:
                setTileColorMode(TileColorMode.WATER_LEVEL);
                return true;
            case Input.Keys.NUM_5:
                setTileColorMode(TileColorMode.WATER_TRAVERSED);
                return true;
            case Input.Keys.NUM_6:
                setTileColorMode(TileColorMode.BIOME);
                return true;
        }

        // Forward everything else to the camera controller
        return cameraController.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        // Forward everything else to the camera controller
        return cameraController.keyUp(keycode);
    }
}
