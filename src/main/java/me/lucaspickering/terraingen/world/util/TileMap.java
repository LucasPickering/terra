package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.lucaspickering.terraingen.world.tile.Tile;

/**
 * A map of {@link Tile}s to some other type. Internally, Tile:T pairs are stored in a map, keyed
 * by the tile's position, but externally this functions as a normal map of Tile:T would. No two
 * tiles can have the same position.
 */
public class TileMap<V> extends AbstractMap<Tile, V> {

    // Internal map
    private final Map<TilePoint, V> map;

    public TileMap() {
        map = new TreeMap<>(); // Uses TilePoint's compareTo method for ordering
    }

    /**
     * Constructs a new {@link TileMap} by copying the given object. This is a shallow
     * copy, meaning the map is copied by the objects within the map are not. You can modify the
     * returned object freely, but not the objects (tiles, etc.) inside it.
     *
     * @param tiles the object to copy
     */
    public TileMap(Map<? extends Tile, ? extends V> tiles) {
        this();
        putAll(tiles);
    }

    @Override
    public V get(Object key) {
        // If the key is a Tile, try to get it from the map
        if (key instanceof Tile) {
            final Tile tile = (Tile) key;
            return map.get(tile.pos());
        }
        return null;// Nothing was retrieved
    }

    @Override
    public V put(Tile tile, V value) {
        return map.put(tile.pos(), value); // Delegation!
    }

    @Override
    public V remove(Object key) {
        // If the key is a Tile, try to remove it from the map
        if (key instanceof Tile) {
            final Tile tile = (Tile) key;
            return map.remove(tile.pos());
        }
        return null; // Nothing was removed
    }

    @NotNull
    @Override
    public Set<Map.Entry<Tile, V>> entrySet() {
        // Not supporting this because I'm lazy and it's not needed anyway
        throw new UnsupportedOperationException();
    }
}
