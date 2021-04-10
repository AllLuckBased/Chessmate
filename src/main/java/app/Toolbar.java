package app;

import components.Settings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import main.Bot;

public class Toolbar extends MenuBar {
    Display display;

    public Toolbar(Display display) {
        this.display = display;

        Menu fileMenu = new Menu("File");
        Menu gameMenu = new Menu("Game");

        // File menu items...
        MenuItem settings = new MenuItem("Settings");
        settings.setOnAction(e -> new Settings(display).show());
        fileMenu.getItems().addAll(settings);

        // Game menu items...
        MenuItem analysisBoard = new MenuItem("Analysis Board");
        analysisBoard.setOnAction(e -> display.showAnalysisBoard());

        MenuItem newGame = new MenuItem("New Game");
        newGame.setOnAction(e -> {
            Thread discordConnection = new Thread(Bot::run);
            discordConnection.setDaemon(true);
            discordConnection.start();
        });

        gameMenu.getItems().addAll(analysisBoard, newGame);

        getMenus().addAll(fileMenu, gameMenu);
    }
}
