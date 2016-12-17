package me.lucaspickering.groundwar.world.generate;

import java.util.Map;
import java.util.Random;

import me.lucaspickering.groundwar.util.TilePoint;
import me.lucaspickering.groundwar.world.Biome;
import me.lucaspickering.groundwar.world.tile.Tile;

public class BiomeGenerator implements Generator {

    // Generation parameters
    private static final int MIN_MOUNTAIN_ELEV = 4;

    @Override
    public void generate(Map<TilePoint, Tile.Builder> world, Random random) {
        for (Tile.Builder builder : world.values()) {
            final Biome biome;
            if (builder.getElevation() >= MIN_MOUNTAIN_ELEV) {
                biome = Biome.MOUNTAIN;
            } else {
                biome = Biome.PLAINS;
            }
            builder.setBiome(biome);
        }
    }
}
