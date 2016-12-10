package me.lucaspickering.groundwar.board.tile;

import me.lucaspickering.groundwar.board.unit.Unit;
import me.lucaspickering.groundwar.util.Colors;
import me.lucaspickering.groundwar.util.Point;

public class MountainTile extends Tile {

    public MountainTile(Point pos) {
        super(pos, Colors.MOUNTAIN_BG, Colors.TILE_OUTLINE);
    }

    @Override
    public boolean isMoveable(Unit mover) {
        return false;
    }
}
