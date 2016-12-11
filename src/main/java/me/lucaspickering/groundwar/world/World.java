package me.lucaspickering.groundwar.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.lucaspickering.groundwar.TerrainGen;
import me.lucaspickering.groundwar.util.Funcs;
import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.tile.Tile;

public class World {

    private static final int X_SIZE = 5;
    private static final int Y_SIZE = 5;

    private final Random random;
    private final Map<TilePoint, Tile> tiles;

    public World() {
        random = TerrainGen.instance().random();
        tiles = new HashMap<>();
        genTiles();
    }

    private void genTiles() {
        // todo generate world here
        int lastElevation = 0;
        for (int x = -X_SIZE; x <= X_SIZE; x++) {
            for (int y = -Y_SIZE; y <= Y_SIZE; y++) {
                for (int z = -X_SIZE; z <= X_SIZE; z++) {
                    if (x + y + z == 0) {
                        final int elevation = Funcs.randomInRange(random,
                                                                  lastElevation - 2,
                                                                  lastElevation + 2);
                        final Tile.Builder builder = Tile.Builder.fromPos(new TilePoint(x, y, z));
                        builder.biome(Biome.PLAINS).elevation(elevation);
                        addTile(builder.build());
                    }
                }
            }
        }
    }

    private void addTile(Tile tile) {
        tiles.put(tile.pos(), tile);
    }

    public Map<TilePoint, Tile> getTiles() {
        return tiles;
    }
}
