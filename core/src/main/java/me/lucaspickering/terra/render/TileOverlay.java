package me.lucaspickering.terra.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import me.lucaspickering.terra.util.Colors;
import me.lucaspickering.terra.world.Tile;

public enum TileOverlay {

    RUNOFF_LEVEL {
        @Override
        public void addRenderables(Tile tile, ModelCache modelCache) {
            if (tile.getRunoffLevel() > 0.0) {
                final Vector3 translate = ChunkModel.getTilePos(tile);
                final Quaternion rotate = new Quaternion(); // No rotation

                // Scale y based on runoff level
                final Vector3 scale = new Vector3(1f, (float) tile.getRunoffLevel(), 1f);

                final ModelInstance modelInst =
                    new ModelInstance(ChunkModel.TILE_MODEL, new Matrix4(translate, rotate, scale));

                // Add color and transparency material attributes
                modelInst.materials.get(0).set(WATER_BLENDING_ATTR);
                modelInst.materials.get(0).set(RUNOFF_COLOR_ATTR);

                modelCache.add(modelInst);
            }
        }
    },
    RUNOFF_EXITS {
        @Override
        public void addRenderables(Tile tile, ModelCache modelCache) {
            Gdx.gl20.glLineWidth(3f);

            // Add a line between this tile and each exit
            final ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            tile.getRunoffPattern().getExits().forEach((exitTile, factor) -> {
                final MeshPartBuilder meshBuilder = modelBuilder.part(
                    null, 1,
                    VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked,
                    new Material());
                meshBuilder.setColor(Color.RED);
                meshBuilder.line(ChunkModel.getTilePos(tile), ChunkModel.getTilePos(exitTile));
            });
            modelCache.add(new ModelInstance(modelBuilder.end()));
        }
    },
    RUNOFF_TERMINALS {
        @Override
        public void addRenderables(Tile tile, ModelCache modelCache) {
            Gdx.gl20.glLineWidth(3f);

            // Add a line between this tile and each exit
            final ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            tile.getRunoffPattern().getTerminals().forEach((terminal, factor) -> {
                final MeshPartBuilder meshBuilder = modelBuilder.part(
                    null, 1,
                    VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked,
                    new Material());
                meshBuilder.setColor(Color.BLUE);
                meshBuilder.line(ChunkModel.getTilePos(tile), ChunkModel.getTilePos(terminal));
            });
            modelCache.add(new ModelInstance(modelBuilder.end()));
        }
    };

    private static final Attribute RUNOFF_COLOR_ATTR = ColorAttribute.createDiffuse(Colors.RUNOFF);
    private static final Attribute WATER_BLENDING_ATTR = new BlendingAttribute(0.25f);

    public abstract void addRenderables(Tile tile, ModelCache modelCache);
}
