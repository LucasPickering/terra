package me.lucaspickering.terra.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Objects;

import me.lucaspickering.utils.Point2;

public class VertexBufferObject {


    private static final int VERTEX_SIZE = 2;

    public enum ColorMode {
        RGB(3) {
            @Override
            float[] getComponents(Color color) {
                color.getColorComponents(colorArray);
                return colorArray;
            }
        },
        RGBA(4) {
            @Override
            float[] getComponents(Color color) {
                color.getComponents(colorArray);
                return colorArray;
            }
        };

        private final int size;
        protected final float[] colorArray;

        ColorMode(int size) {
            this.size = size;
            colorArray = new float[size];
        }

        abstract float[] getComponents(Color color);

        private int bytes() {
            return size * Float.BYTES;
        }
    }

    public static class Builder {

        private int numVertices;
        private ColorMode colorMode = ColorMode.RGB;
        private Runnable drawFunction;

        public Builder setColorMode(ColorMode colorMode) {
            Objects.requireNonNull(colorMode);
            this.colorMode = colorMode;
            return this;
        }

        public Builder setNumVertices(int numVertices) {
            this.numVertices = numVertices;
            return this;
        }

        public Builder setDrawFunction(Runnable drawFunction) {
            this.drawFunction = drawFunction;
            return this;
        }

        public VertexBufferObject build() {
            if (drawFunction == null) {
                throw new IllegalStateException("Draw function cannot be null");
            }
            return new VertexBufferObject(numVertices, colorMode, drawFunction);
        }
    }

    private final int numVertices;
    private final ColorMode colorMode;
    private final Runnable drawFunction;

    private int vertexHandle;
    private final FloatBuffer vertexBuffer;

    private final FloatBuffer colorBuffer;
    private int colorHandle;

    private VertexBufferObject(int numVertices, ColorMode colorMode, Runnable drawFunction) {
        this.numVertices = numVertices;
        this.colorMode = colorMode;
        this.drawFunction = drawFunction;

        vertexBuffer = BufferUtils.createFloatBuffer(VERTEX_SIZE * numVertices);
        colorBuffer = BufferUtils.createFloatBuffer(colorMode.size * numVertices);
    }

    public int getVertexHandle() {
        return vertexHandle;
    }

    public void setVertexHandle(int vertexHandle) {
        this.vertexHandle = vertexHandle;
    }

    public int getColorHandle() {
        return colorHandle;
    }

    public void setColorHandle(int colorHandle) {
        this.colorHandle = colorHandle;
    }

    public void addVertex(Point2 point) {
        vertexBuffer.put((float) point.x());
        vertexBuffer.put((float) point.y());
    }

    public void addColor(Color color) {
        colorBuffer.put(colorMode.getComponents(color)); // Add the color to the buffer
    }

    public void bindVertexBuffer(int usage) {
        vertexHandle = bindBuffer(vertexBuffer, usage);
    }

    public void bindColorBuffer(int usage) {
        colorHandle = bindBuffer(colorBuffer, usage);
    }

    private int bindBuffer(FloatBuffer buffer, int usage) {
        buffer.rewind(); // Reset the buffer before binding
        final int handle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, handle);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, usage);
        GL30.glBindVertexArray(0);
        return handle;
    }

    /**
     * Sets the color at the given offset. Returns the number of bytes set. The color after this
     * one in the buffer will be located at the given offset plus the return value of this call.
     *
     * @param offset the position of the color in the buffer (in bytes)
     * @param color  the new color to use
     * @return the number of bytes set
     */
    public int setVertexColor(long offset, Color color) {
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, colorMode.getComponents(color));
        return colorMode.bytes();
    }

    public void draw() {
        // Set up the vertex buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexHandle);
        GL11.glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, 0, 0L);

        // Set up the color buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorHandle);
        GL11.glColorPointer(colorMode.size, GL11.GL_FLOAT, 0, 0L);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        drawFunction.run(); // Do the actual draw

        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }
}
