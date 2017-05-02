package me.lucaspickering.terra.world.generate;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;

public class RiverGenerator extends Generator {

    private static final double RIVER_THRESHOLD = 10.0;

    public RiverGenerator(World world, Random random) {
        super(world, random);
    }

    @Override
    public void generate() {
        world().getContinents().parallelStream().forEach(this::generateForContinent);
    }

    private void generateForContinent(Continent continent) {
        final TileSet riverTiles = continent.getTiles().parallelStream()
            .filter(t -> t.getWaterTraversed() >= RIVER_THRESHOLD)
            .collect(Collectors.toCollection(TileSet::new));
        final List<Tile> sortedRiverTiles = riverTiles.stream()
            .sorted((t1, t2) -> Integer.compare(t2.elevation(), t1.elevation()))
            .collect(Collectors.toList());

        for (Tile tile : sortedRiverTiles) {
            for (Direction dir : Direction.values()) {
                tile.addRiverConnection(dir, Tile.RiverConnection.ENTRY);
            }
        }
    }
}
