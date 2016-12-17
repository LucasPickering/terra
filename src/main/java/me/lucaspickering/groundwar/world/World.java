package me.lucaspickering.groundwar.world;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.groundwar.TerrainGen;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.generate.BiomeGenerator;
import me.lucaspickering.groundwar.world.generate.Generator;
import me.lucaspickering.groundwar.world.generate.PeakGenerator;
import me.lucaspickering.groundwar.world.tile.Tile;

public class World {

    // Board size
    private static final int X_SIZE = 5, Y_SIZE = 5, Z_SIZE = 5;

    private static final Generator[] GENERATORS = new Generator[]{
        new PeakGenerator(),
        new BiomeGenerator(),
    };

    private final Random random;
    private final Map<TilePoint, Tile> tiles;

    public World() {
        random = TerrainGen.instance().random();
        tiles = new HashMap<>();
        genTiles();
    }

    private void genTiles() {
        // Temporary map to hole Tile builders until they're ready to be built
        final Map<TilePoint, Tile.Builder> builders = new HashMap<>();
        // Fill out the board with builders, to be populated with biome/elev/etc. later on
        for (int x = -X_SIZE; x <= X_SIZE; x++) {
            for (int y = -Y_SIZE; y <= Y_SIZE; y++) {
                for (int z = -Z_SIZE; z <= Z_SIZE; z++) {
                    if (x + y + z == 0) {
                        final TilePoint pos = new TilePoint(x, y, z);
                        builders.put(pos, Tile.Builder.fromPos(pos));
                    }
                }
            }
        }

        // Apply each generator in sequence
        Arrays.stream(GENERATORS).forEach(gen -> gen.generate(builders, random));

        // Build each tile and put it into the final map
        builders.forEach((pos, builder) -> tiles.put(pos, builder.build()));
    }

    /**
     * Gets the world's copy of tiles. This is NOT a copy, so DO NOT MODIFY IT.
     *
     * @return the world's copy of tiles
     */
    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }
}
