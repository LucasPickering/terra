package me.lucaspickering.terra.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.lucaspickering.terra.input.CameraController;
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
        final long startTime = System.currentTimeMillis(); // We're timing this
        worldHandler.getWorld().getChunks().forEach(c -> chunkModels.put(c, new ChunkModel(c)));
        final long elapsedTime = System.currentTimeMillis() - startTime; // Stop the timer
        logger.log(Level.INFO, String.format("Initializing world models took %d ms", elapsedTime));
    }

    @Override
    public void draw(Point2 mousePos) {
        cameraController.update();

        // Render each chunk
        modelBatch.begin(camera);
        modelBatch.render(chunkModels.values(), environment);
        modelBatch.end();
    }

    private void updateAllTileColors() {
        final long startTime = System.currentTimeMillis();
        // TODO
        final long endTime = System.currentTimeMillis();
        logger.log(Level.FINER, String.format("Color update took %d ms", endTime - startTime));
    }

    private void regenerateWorld() {
        worldHandler.generate();
    }

    private void stepWorld() {
        // Step then refresh the world
        worldHandler.step();
        updateAllTileColors();
    }

    @Override
    public void dispose() {
        chunkModels.values().forEach(ChunkModel::dispose);
    }
}
