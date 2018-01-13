package me.lucaspickering.terra.world.generate;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terra.world.Biome;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;

/**
 * Turns all land tiles that border ocean/coast and that are below some elevation threshold into
 * beach or cliffs.
 */
public class CoastGenerator extends Generator {

    // Any tile <= this elevation will become beach, others will keep their normal biome
    private static final int MAX_BEACH_ELEV = 50;

    // Biomes that can get beaches adjacent to them
    private static final Set<Biome> BEACHABLE_BIOMES = EnumSet.of(Biome.OCEAN, Biome.COAST);

    public CoastGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        final TileSet worldTiles = world().getTiles();
        for (Tile tile : worldTiles) {
            // If this tile is land and within our elevation bound, check the adjacent tiles, and
            // if there is an ocean (or similar) tile adjacent, make a beach.
            if (tile.biome().isLand()) {
                for (Tile adj : worldTiles.getAdjacentTiles(tile.pos()).values()) {
                    if (BEACHABLE_BIOMES.contains(adj.biome())) {
                        // If this tile is under the elevation threshold, make it a beach
                        if (tile.elevation() <= MAX_BEACH_ELEV) {
                            tile.setBiome(Biome.BEACH);
                        }
                        break; // Done with this tile
                    }
                }
            }
        }
    }
}
