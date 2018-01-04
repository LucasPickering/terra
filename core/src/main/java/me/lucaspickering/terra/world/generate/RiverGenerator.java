package me.lucaspickering.terra.world.generate;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import me.lucaspickering.terra.util.Direction;
import me.lucaspickering.terra.world.Continent;
import me.lucaspickering.terra.world.Tile;
import me.lucaspickering.terra.world.World;
import me.lucaspickering.terra.world.util.TileSet;

public class RiverGenerator extends Generator {

    private static final double RIVER_THRESHOLD = 7.0;

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

        while (!riverTiles.isEmpty()) {
            Tile firstTile = riverTiles.stream()
                .max(Comparator.comparingDouble(Tile::getWaterTraversed))
                .orElseThrow(RuntimeException::new);
            riverTiles.remove(firstTile);

            Map.Entry<Direction, Tile> nextTileEntry = getNextTile(firstTile);
            while (nextTileEntry != null) {
                final Direction dir = nextTileEntry.getKey();
                final Tile nextTile = nextTileEntry.getValue();
                firstTile.addRiverConnection(dir, Tile.RiverConnection.EXIT);
                if (nextTile.biome().isLand()) {
                    nextTile.addRiverConnection(dir.opposite(), Tile.RiverConnection.ENTRY);
                    riverTiles.remove(nextTile);

                    firstTile = nextTile;
                } else {
                    break;
                }

                nextTileEntry = getNextTile(firstTile);
            }
        }
    }

    private Map.Entry<Direction, Tile> getNextTile(Tile tile) {
        final Map<Direction, Tile> adjTiles = world().getTiles().getAdjacentTiles(tile.pos());
        Map.Entry<Direction, Tile> nextTile;
        nextTile = adjTiles.entrySet().stream()
            .filter(p -> p.getValue().biome().isLand() &&
                         p.getValue().elevation() < tile.elevation())
            .max(Comparator.comparingDouble(p -> p.getValue().getWaterTraversed()))
            .orElse(null);
        if (nextTile != null) {
            return nextTile;
        }

        nextTile = adjTiles.entrySet().stream()
            .filter(p -> p.getValue().biome().isWater())
            .findFirst()
            .orElse(null);
        return nextTile;
    }
}
