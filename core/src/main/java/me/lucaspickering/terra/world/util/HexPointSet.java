package me.lucaspickering.terra.world.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import me.lucaspickering.terra.world.Tile;

public class HexPointSet<E extends HexPointable> extends AbstractSet<E> {

    // Internal map
    private final Map<HexPoint, E> map;

    public HexPointSet() {
        map = new TreeMap<>(); // Uses HexPoint's compareTo method for ordering
    }

    /**
     * Constructs a new {@link TileSet} by copying the values in the given object. This is a shallow
     * copy, meaning the objects are put in this collection without being copied. You can modify the
     * returned object freely, but be careful about modifying the tiles inside it.
     *
     * @param tiles the object to copy
     */
    public HexPointSet(Collection<? extends E> tiles) {
        this();
        addAll(tiles);
    }

    /**
     * Constructs a new set backed by the given map. No copying is done.
     *
     * @param map the map to back this object
     */
    protected HexPointSet(Map<HexPoint, E> map) {
        this.map = map;
    }

    protected Map<HexPoint, E> immutableInternalMap() {
        return Collections.unmodifiableMap(map);
    }

    public E getByPoint(HexPoint point) {
        return map.get(point);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        // Cast o to a tile and check if its position is in the map
        return o instanceof HexPointable && containsPoint(((HexPointable) o).toHexPoint());
    }

    public boolean containsPoint(HexPoint point) {
        return getByPoint(point) != null;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return map.values().iterator();
    }

    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e);
        map.put(e.toHexPoint(), e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Tile) {
            final Tile tile = (Tile) o;
            return removeByPoint(tile.pos());
        }
        return false;
    }

    public boolean removeByPoint(HexPoint point) {
        return map.remove(point) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Creates a shallow immutable copy of this set. The internal objects are still mutable, but no
     * objects can be added to or removed from the copy.
     *
     * @return an shallow immutable copy of this set
     */
    public HexPointSet<E> immutableCopy() {
        return new HexPointSet<>(immutableInternalMap());
    }
}
