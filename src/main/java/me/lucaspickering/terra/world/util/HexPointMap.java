package me.lucaspickering.terra.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.lucaspickering.utils.Pair;

public class HexPointMap<K extends HexPointable, V> extends AbstractMap<K, V> {

    private class EntrySet extends AbstractSet<Entry<K, V>> {

        private final Set<Entry<HexPoint, Pair<K, V>>> backingEntrySet;

        private EntrySet() {
            this.backingEntrySet = map.entrySet();
        }

        @NotNull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {

                // Internal iterator that backs this one
                private Iterator<Entry<HexPoint, Pair<K, V>>> iter = backingEntrySet.iterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    // Get the next value in the backing iterator
                    final Entry<HexPoint, Pair<K, V>> next = iter.next();
                    final Pair<K, V> pair = next.getValue();

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
    private final Map<HexPoint, Pair<K, V>> map;

    // Cache this so we only need to make it once
    private Set<Entry<K, V>> entrySet;

    public HexPointMap() {
        map = new TreeMap<>(); // Uses HexPoint's compareTo method for ordering
    }

    protected HexPointMap(Map<HexPoint, Pair<K, V>> map) {
        this.map = map;
    }

    private V extractValue(Pair<K, V> pair) {
        // If the pair isn't null, get the value out, otherwise just return null
        if (pair != null) {
            return pair.second();
        }
        return null;
    }

    @Override
    public V get(Object key) {
        // If the key is a Tile, try to get it from the map
        if (key instanceof HexPointable) {
            final HexPointable hp = (HexPointable) key;
            return getByPoint(hp.toHexPoint());
        }
        return null; // Nothing was retrieved
    }

    public V getByPoint(HexPoint point) {
        final Pair<K, V> retrieved = map.get(point);
        return extractValue(retrieved);
    }

    @Override
    public V put(K key, V value) {
        final Pair<K, V> evicted = map.put(key.toHexPoint(), new Pair<>(key, value));
        return extractValue(evicted);
    }

    @Override
    public V remove(Object key) {
        // If the key is a Tile, try to remove it from the map
        if (key instanceof HexPointable) {
            final HexPointable hp = (HexPointable) key;
            return removeByPoint(hp.toHexPoint());
        }
        return null; // Nothing was removed
    }

    public V removeByPoint(HexPoint point) {
        final Pair<K, V> removed = map.remove(point);
        return extractValue(removed);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        // If entrySet isn't already initialized, do that now
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    /**
     * Creates a shallow immutable copy of this map. Items can no longer be added or removed, but
     * they can be modified.
     *
     * @return the immutable copy
     */
    public HexPointMap<K, V> immutableCopy() {
        return new HexPointMap<>(Collections.unmodifiableMap(map));
    }
}
