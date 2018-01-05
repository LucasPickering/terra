package me.lucaspickering.terra.render.screen;

public class ScreenHandler {

    // Only null before initialization
    private Screen currentScreen;

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(Screen currentScreen) {
        // Release resources from current screen before setting new one
        if (this.currentScreen != null) {
            this.currentScreen.dispose();
        }
        this.currentScreen = currentScreen;
    }
}
