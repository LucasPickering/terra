package me.lucaspickering.terraingen.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.lucaspickering.terraingen.world.Tile;
import me.lucaspickering.utils.Pair;

/**
 * A map of {@link Tile}s to some other type. Internally, Tile:T pairs are stored in a map, keyed
 * by the tile's position, but externally this functions as a normal map of Tile:T would. No two
 * tiles can have the same position.
 */
public class TileMap<V> extends AbstractMap<Tile, V> {

    private class EntrySet extends AbstractSet<Entry<Tile, V>> {

        private final Set<Entry<HexPoint, Pair<Tile, V>>> backingEntrySet;

        private EntrySet() {
            this.backingEntrySet = map.entrySet();
        }

        @NotNull
        @Override
        public Iterator<Entry<Tile, V>> iterator() {
            return new Iterator<Entry<Tile, V>>() {

                // Internal iterator that backs this one
                private Iterator<Entry<HexPoint, Pair<Tile, V>>> iter = backingEntrySet.iterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry<Tile, V> next() {
                    // Get the next value in the backing iterator
                    final Entry<HexPoint, Pair<Tile, V>> next = iter.next();
                    final Pair<Tile, V> pair = next.getValue();

                    // Extract the values we ant from the next value and return them in an entry
                    return new SimpleEntry<>(pair.first(), pair.second());
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            // Not supporting this because I'm lazy
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            // Not supporting this because I'm lazy
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }
    }

    // Internal map
    private final Map<HexPoint, Pair<Tile, V>> map;

    // Cache this so we only need to make it once
    private Set<Entry<Tile, V>> entrySet;

    public TileMap() {
        map = new TreeMap<>(); // Uses HexPoint's compareTo method for ordering
    }

    private TileMap(Map<HexPoint, Pair<Tile, V>> map) {
        this.map = map;
    }

    private V extractValue(Pair<Tile, V> pair) {
        // If the pair isn't null, get the value out, otherwise just return null
        if (pair != null) {
            return pair.second();
        }
        return null;
    }

    @Override
    public V get(Object key) {
        // If the key is a Tile, try to get it from the map
        if (key instanceof Tile) {
            final Tile tile = (Tile) key;
            final Pair<Tile, V> retrieved = map.get(tile.pos());
            return extractValue(retrieved);
        }
        return null; // Nothing was retrieved
    }

    @Override
    public V put(Tile tile, V value) {
        final Pair<Tile, V> evicted = map.put(tile.pos(), new Pair<>(tile, value));
        return extractValue(evicted);
    }

    @Override
    public V remove(Object key) {
        // If the key is a Tile, try to remove it from the map
        if (key instanceof Tile) {
            final Tile tile = (Tile) key;
            final Pair<Tile, V> removed = map.remove(tile.pos());
            return extractValue(removed);
        }
        return null; // Nothing was removed
    }

    @NotNull
    @Override
    public Set<Map.Entry<Tile, V>> entrySet() {
        // If entrySet isn't already initialized, do that now
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    /**
     * Creates a deep immutable copy of this object. Each internal tile will also be copied and
     * made immutable.
     *
     * @return the immutable copy
     */
    public TileMap<V> immutableCopy() {
        return new TileMap<>(Collections.unmodifiableMap(map));
    }
}
