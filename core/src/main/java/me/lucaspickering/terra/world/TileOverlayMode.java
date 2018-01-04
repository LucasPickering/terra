package me.lucaspickering.terra.world;

import java.awt.Color;


/**
 * Specifies different modes by which tiles can be displayed. Each tile ahs a different input scale
 * and a different output gradient.
 */
public enum TileOverlayMode {

    NONE {
        @Override
        public Color getColor(Tile tile) {
            return null;
        }
    },
    CONTINENT {
        @Override
        public Color getColor(Tile tile) {
            final Continent continent = tile.getContinent();
            return continent != null ? continent.getOverlayColor() : null;
        }
    },
    CHUNK {
        @Override
        public Color getColor(Tile tile) {
            return tile.getChunk().getOverlayColor();
        }
    };

    public abstract Color getColor(Tile tile);
}
