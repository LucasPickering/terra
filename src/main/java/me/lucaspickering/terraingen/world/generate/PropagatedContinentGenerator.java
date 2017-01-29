package me.lucaspickering.terraingen.world.generate;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;

import me.lucaspickering.terraingen.util.Direction;
import me.lucaspickering.terraingen.util.Funcs;
import me.lucaspickering.terraingen.world.Biome;
import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.terraingen.world.World;
import me.lucaspickering.terraingen.world.util.TilePoint;
import me.lucaspickering.terraingen.world.util.TileSet;

public class PropagatedContinentGenerator implements Generator {

    private static final int MAX_CHANGE = 5;
    private static final float STAY_CHANCE = 0.6f;

    private World world;
    private Random random;

    @Override
    public void generate(World world, Random random) {
        this.world = world;
        this.random = random;
        final TileSet worldTiles = world.getTiles();
        final Tile origin = Funcs.randomFromCollection(random, worldTiles);
        applyAndPropagate(world.getTiles(), origin, this::setElevation, 0);
    }

    private int setElevation(Tile tile, int lastElev) {
        int delta = random.nextInt(MAX_CHANGE);
        if (lastElev < 0) {
            delta *= -1;
        }
        if (Funcs.weightedChance(random, STAY_CHANCE)) {
            delta *= -1;
        }
        final int elev = lastElev + delta;
        tile.setElevation(elev);
        tile.setBiome(Biome.FOREST);

        return elev;
    }

    private <T> void applyAndPropagate(TileSet tiles, Tile origin,
                                       BiFunction<Tile, T, T> function, T initialResult) {
        final TileSet unvisitedTiles = new TileSet(tiles);
        applyAndPropagateHelper(origin, null, unvisitedTiles, function, initialResult);
    }

    private <T> void applyAndPropagateHelper(Tile tile, Direction sourceDir, TileSet unvisitedTiles,
                                             BiFunction<Tile, T, T> tileFunction, T lastResult) {
        final T result = tileFunction.apply(tile, lastResult);

        // Apply to next tile(s)
        unvisitedTiles.remove(tile);
        for (Direction nextDir : getNextDirections(sourceDir)) {
            final TilePoint nextPoint = nextDir.shift(tile.pos());
            final Tile nextTile = unvisitedTiles.getByPoint(nextPoint);
            if (nextTile != null) {
                applyAndPropagateHelper(nextTile, nextDir, unvisitedTiles, tileFunction, result);
            }
        }
    }

    private Set<Direction> getNextDirections(Direction sourceDir) {
        if (sourceDir == null) {
            return EnumSet.allOf(Direction.class);
        }

        // Always continue in the same direction
        final Set<Direction> rv = EnumSet.of(sourceDir);

        // Add additional directions if necessary
        switch (sourceDir) {
            case NORTH:
                rv.add(Direction.NORTHEAST);
                rv.add(Direction.NORTHWEST);
                break;
            case NORTHEAST:
                rv.add(Direction.SOUTHEAST);
                break;
            case NORTHWEST:
                rv.add(Direction.SOUTHWEST);
                break;
            case SOUTH:
                rv.add(Direction.SOUTHEAST);
                rv.add(Direction.SOUTHWEST);
                break;
        }

        return rv;
    }
}
