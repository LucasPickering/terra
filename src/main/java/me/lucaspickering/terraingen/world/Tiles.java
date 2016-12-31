package me.lucaspickering.terraingen.world;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

public class Tiles extends AbstractSet<Tile> {

    // Internal map
    private final Map<TilePoint, Tile> map;

    public Tiles() {
        map = new HashMap<>();
    }

    /**
     * Constructs a new {@code Tiles} by copying the map in the given object. This is a shallow
     * copy, meaning the map is copied by the objects within the map are not. You can modify the
     * returned object freely, but not the objects (tiles, etc.) inside it.
     *
     * @param tiles the object to copy
     */
    public Tiles(Tiles tiles) {
        map = new HashMap<>(tiles.map);
    }

    /**
     * Constructs a new {@code Tiles} backed by the given map. No copying is done.
     *
     * @param map the map to back this object
     */
    private Tiles(Map<TilePoint, Tile> map) {
        this.map = map;
    }

    /**
     * Creates a deep immutable copy of this object. Each internal tile will also be copied and
     * made immutable.
     *
     * @return the immutable copy
     */
    public Tiles immutableCopy() {
        // Turn the map of point:Tile into a map of point:ImmutableTile
        final Map<TilePoint, ImmutableTile> tiles = new HashMap<>();
        for (Tile tile : map.values()) {
            tiles.put(tile.pos(), new ImmutableTile(tile));
        }
        return new Tiles(Collections.unmodifiableMap(tiles));
    }

    public Tile getByPoint(TilePoint point) {
        return map.get(point);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        // Cast o to a tile and check if its position is in the map
        return o instanceof Tile && map.containsKey(((Tile) o).pos());
    }

    public boolean containsPoint(TilePoint point) {
        return map.containsKey(point);
    }

    @NotNull
    @Override
    public Iterator<Tile> iterator() {
        return map.values().iterator();
    }

    @Override
    public boolean add(Tile tile) {
        return map.put(tile.pos(), tile) != null;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Tile) {
            final Tile tile = (Tile) o;
            return map.remove(tile.pos()) != null;
        }
        return false;
    }

    public boolean removePoint(TilePoint point) {
        return map.remove(point) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
