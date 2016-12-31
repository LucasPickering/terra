package me.lucaspickering.terraingen.world;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.lucaspickering.terraingen.util.TilePoint;
import me.lucaspickering.terraingen.world.tile.ImmutableTile;
import me.lucaspickering.terraingen.world.tile.Tile;

public class Tiles implements Map<TilePoint, Tile> {

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

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Tile get(Object key) {
        return map.get(key);
    }

    @Override
    public Tile put(@Flow(target = "this.keys", targetIsContainer = true) TilePoint key,
                    @Flow(target = "this.values", targetIsContainer = true) Tile value) {
        return map.put(key, value);
    }

    /**
     * Puts the given tile in this map. The tile's position ({@link Tile#pos()} is used as the
     * key, and the tile itself is the value.
     *
     * @param tile the tile to add to the map
     * @return the evicted value (or {@code null} if nothing was evicted)
     */
    public Tile putTile(Tile tile) {
        return put(tile.pos(), tile);
    }

    @Override
    public Tile remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends TilePoint, ? extends Tile> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<TilePoint> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<Tile> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<TilePoint, Tile>> entrySet() {
        return map.entrySet();
    }
}
