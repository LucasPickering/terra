package me.lucaspickering.terra.world.util;

/**
 * A {@link HexPointable} is an object that has some way of being converted to a {@link HexPoint},
 * which can then be used as a unique key for that object.
 */
@FunctionalInterface
public interface HexPointable {

    HexPoint toHexPoint();

}
