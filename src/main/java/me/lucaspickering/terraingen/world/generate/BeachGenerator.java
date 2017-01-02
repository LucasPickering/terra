package me.lucaspickering.terraingen.world.generate;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Tiles;
import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * Turns all land tiles that border ocean/coast and that are below some elevation threshold into
 * beach.
 */
public class BeachGenerator implements Generator {

    // Any tile <= this elevation that borders ocean will become beach
    private static final int MAX_BEACH_ELEV = 5;

    // Biomes that can get beaches adjacent to them
    public static final Set<Biome> BEACHABLE_BIOMES = EnumSet.of(Biome.OCEAN, Biome.COAST);

    @Override
    public void generate(Tiles tiles, Random random) {
        for (Tile tile : tiles) {
            // If this tile is land and within our elevation bound, check the adjacent tiles, and
            // if there is an ocean (or similar) tile adjacent, make a beach.
            if (tile.biome().isLand() && tile.elevation() <= MAX_BEACH_ELEV) {
                for (Tile adj : tile.adjacents().values()) {
                    if (BEACHABLE_BIOMES.contains(adj.biome())) {
                        tile.setBiome(Biome.BEACH);
                        break; // Done with this tile
                    }
                }
            }
        }
    }
}
