package me.lucaspickering.terra.world.generate;

import java.util.Random;

import me.lucaspickering.terra.world.Biome;
import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;

public class LakeGenerator extends Generator {

    private static final double LAKE_THRESHOLD = 3.0;

    public LakeGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        world().getContinents().parallelStream().forEach(this::generateForContinent);
    }

    private void generateForContinent(Continent continent) {
        for (Tile tile : continent.getTiles()) {
            if (tile.getRunoffLevel() >= LAKE_THRESHOLD) {
                tile.setBiome(Biome.LAKE);
            }
        }
    }
}
