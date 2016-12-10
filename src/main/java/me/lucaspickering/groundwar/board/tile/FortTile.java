package me.lucaspickering.groundwar.board.tile;

import me.lucaspickering.groundwar.board.Player;
import me.lucaspickering.groundwar.util.Point;

public class FortTile extends Tile {

    public FortTile(Point pos, Player owner) {
        super(pos, owner);
    }

    @Override
    public boolean shouldGameEnd() {
        return hasUnit() && getUnit().hasFlag() && getUnit().getFlag().getOwner() != getOwner();
    }
}
