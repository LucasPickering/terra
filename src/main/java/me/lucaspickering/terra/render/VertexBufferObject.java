package me.lucaspickering.terra.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.awt.Color;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.lucaspickering.utils.Point2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

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

        private final List<Point2> vertices = new LinkedList<>();
        private final List<Color> colors = new LinkedList<>();
        private final List<Integer> indices = new LinkedList<>();
        private ColorMode colorMode = ColorMode.RGB;
        private Runnable drawFunction;
        private int vertexUsage = GL_STATIC_DRAW;
        private int colorUsage = GL_STATIC_DRAW;
        private int indexUsage = GL_STATIC_DRAW;

        public Builder addVertex(Point2 vertex, Color color) {
            vertices.add(vertex);
            colors.add(color);
            return this;
        }

        public Builder addIndex(int index) {
            indices.add(index);
            return this;
        }

        public int getNumVertices() {
            return vertices.size();
        }

        public int getNumIndices() {
            return indices.size();
        }

        public Builder colorMode(ColorMode colorMode) {
            Objects.requireNonNull(colorMode);
            this.colorMode = colorMode;
            return this;
        }

        public Builder drawFunction(Runnable drawFunction) {
            this.drawFunction = drawFunction;
            return this;
        }

        public Builder vertexUsage(int vertexUsage) {
            this.vertexUsage = vertexUsage;
            return this;
        }

        public Builder colorUsage(int colorUsage) {
            this.colorUsage = colorUsage;
            return this;
        }

        public Builder indexUsage(int indexUsage) {
            this.indexUsage = indexUsage;
            return this;
        }

        public VertexBufferObject build() {
            if (drawFunction == null) {
                throw new IllegalStateException("Draw function cannot be null");
            }

            return new VertexBufferObject(vertices, colors, indices, colorMode, drawFunction,
                                          vertexUsage, colorUsage, indexUsage);
        }
    }

    @FunctionalInterface
    private interface BufferDataFunction<T extends Buffer> {

        void bufferData(int target, T buffer, int usage);
    }

    private final ColorMode colorMode;
    private final Runnable drawFunction;

    private final int vertexHandle;
    private final int colorHandle;
    private final int indexHandle;
//    private final int vaoHandle;

    private VertexBufferObject(List<Point2> vertices, List<Color> colors, List<Integer> indices,
                               ColorMode colorMode, Runnable drawFunction,
                               int vertexUsage, int colorUsage, int indexUsage) {
        this.colorMode = colorMode;
        this.drawFunction = drawFunction;

        // Allocate buffers
        final FloatBuffer vertexBuffer =
            BufferUtils.createFloatBuffer(VERTEX_SIZE * vertices.size());
        final FloatBuffer colorBuffer =
            BufferUtils.createFloatBuffer(colorMode.size * colors.size());
        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());

        // Populate buffers
        vertices.forEach(v -> {
            vertexBuffer.put((float) v.x());
            vertexBuffer.put((float) v.y());
//            vertexBuffer.put((float) v.z());
        });
        colors.forEach(c -> colorBuffer.put(colorMode.getComponents(c)));
        indices.forEach(indexBuffer::put);

        // Initialize VAO
//        vaoHandle = glGenVertexArrays();
//        glBindVertexArray(vaoHandle);

        // Load data from buffers
        vertexHandle = loadBufferData(GL15::glBufferData, vertexBuffer, GL_ARRAY_BUFFER, vertexUsage);
        colorHandle = loadBufferData(GL15::glBufferData, colorBuffer, GL_ARRAY_BUFFER, colorUsage);
        indexHandle = loadBufferData(GL15::glBufferData, indexBuffer, GL_ELEMENT_ARRAY_BUFFER,
                                     indexUsage);

        // Unbind buffers
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public int getVertexHandle() {
        return vertexHandle;
    }

    public int getColorHandle() {
        return colorHandle;
    }

    public int getIndexHandle() {
        return indexHandle;
    }

    /**
     * Load data from the given buffer. The buffer is bound, then its data is loaded to VRAM.
     *
     * @param bufferDataFunc the function used to load the buffer's data
     * @param buffer         the buffer to bind
     * @param target         the target parameter for {@link GL15#glBindBuffer}
     * @param usage          the usage parameter for the buffer data function
     * @param <T>            the type of the buffer
     * @return the handle of the bound buffer
     */
    private <T extends Buffer> int loadBufferData(BufferDataFunction<T> bufferDataFunc, T buffer,
                                                  int target, int usage) {
        buffer.rewind(); // Reset the position in the buffer before binding
        final int handle = glGenBuffers();
        glBindBuffer(target, handle);
        bufferDataFunc.bufferData(target, buffer, usage);
        return handle;
    }

    /**
     * Sets the color at the given offset. Returns the number of bytes set. The color after this one
     * in the buffer will be located at the given offset plus the return value of this call.
     *
     * @param offset the position of the color in the buffer (in bytes)
     * @param color  the new color to use
     * @return the number of bytes set
     */
    public int setVertexColor(long offset, Color color) {
        glBufferSubData(GL_ARRAY_BUFFER, offset, colorMode.getComponents(color));
        return colorMode.bytes();
    }

    public void draw() {
        // Set up the vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
        glVertexPointer(VERTEX_SIZE, GL_FLOAT, 0, 0L);

        // Set up the color buffer
        glBindBuffer(GL_ARRAY_BUFFER, colorHandle);
        glColorPointer(colorMode.size, GL_FLOAT, 0, 0L);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

//        glBindVertexArray(vaoHandle);
//        glEnableVertexAttribArray(0);
//        glEnableVertexAttribArray(1);

        drawFunction.run(); // Do the actual draw

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

//        glDisableVertexAttribArray(0);
//        glDisableVertexAttribArray(1);
//        glBindVertexArray(0);
    }

    public void cleanup() {
//        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vertexHandle);
        glDeleteBuffers(colorHandle);
        glDeleteBuffers(indexHandle);

        // Delete the VAO
//        glBindVertexArray(0);
//        glDeleteVertexArrays(vaoHandle);
    }
}
