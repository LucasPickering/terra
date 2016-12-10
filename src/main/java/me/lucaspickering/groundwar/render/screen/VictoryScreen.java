package me.lucaspickering.groundwar.render.screen;

import org.lwjgl.opengl.GL11;

import me.lucaspickering.groundwar.board.Board;
import me.lucaspickering.groundwar.board.PlayerInfo;
import me.lucaspickering.groundwar.render.HorizAlignment;
import me.lucaspickering.groundwar.render.VertAlignment;
import me.lucaspickering.groundwar.render.event.MouseButtonEvent;
import me.lucaspickering.groundwar.render.screen.gui.Button;
import me.lucaspickering.groundwar.render.screen.gui.GuiElement;
import me.lucaspickering.groundwar.util.Constants;
import me.lucaspickering.groundwar.util.Point;

public class VictoryScreen extends MainScreen {

    private static final int MESSAGE_Y = 350;
    private static final int MAIN_MENU_BUTTON_Y = 1000;
    private static final int EXIT_BUTTON_Y = 1200;

    private final Board board;
    private final Button menuButton;
    private final Button exitButton;

    public VictoryScreen(Board board) {
        this.board = board;
        addGuiElement(menuButton = new Button("Main Menu", new Point(center.getX(),
                                                                     MAIN_MENU_BUTTON_Y),
                                              HorizAlignment.CENTER, VertAlignment.TOP));
        addGuiElement(exitButton = new Button("Exit Game", new Point(center.getX(),
                                                                     EXIT_BUTTON_Y),
                                              HorizAlignment.CENTER, VertAlignment.TOP));
    }

    @Override
    public void draw(Point mousePos) {
        super.draw(mousePos);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        PlayerInfo winnerInfo = board.getWinner().getInfo();
        renderer().drawString(
            Constants.FONT_SIZE_TITLE,
            String.format("%s wins\nin %d turns!", winnerInfo.displayName, board.getTurnCount()),
            center.getX(), MESSAGE_Y, winnerInfo.primaryColor,
            HorizAlignment.CENTER, VertAlignment.CENTER);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void onElementClicked(MouseButtonEvent event, GuiElement element) {
        if (element == menuButton) {
            setNextScreen(new MainMenuScreen());
        } else if (element == exitButton) {
            setNextScreen(null);
        }
    }
}
