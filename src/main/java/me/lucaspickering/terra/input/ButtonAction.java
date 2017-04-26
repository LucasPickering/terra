package me.lucaspickering.terra.input;

public enum ButtonAction {
    // These have to be in the same order as the values of the GLFW constants
    RELEASE, PRESS, REPEAT;

    public static ButtonAction getByGlfwCode(int glfwCode) {
        if (glfwCode < 0 || glfwCode >= values().length) {
            throw new IndexOutOfBoundsException("Unknown GLFW action code: " + glfwCode);
        }
        return values()[glfwCode];
    }
}