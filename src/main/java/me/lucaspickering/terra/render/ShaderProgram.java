package me.lucaspickering.terra.render;

import java.io.IOException;

import me.lucaspickering.terra.util.Constants;
import me.lucaspickering.terra.util.Funcs;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader");
        }
    }

    public void loadVertexShader(String shaderFile) {
        vertexShaderId = loadShader(GL_VERTEX_SHADER, shaderFile);
    }

    public void loadFragmentShader(String shaderFile) {
        fragmentShaderId = loadShader(GL_FRAGMENT_SHADER, shaderFile);
    }

    private int loadShader(int type, String shaderFile) {
        final String shaderCode;
        try {
            shaderCode = Funcs.loadTextResource(Constants.SHADER_PATH, shaderFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + type);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException(
                "Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException(
                "Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}

