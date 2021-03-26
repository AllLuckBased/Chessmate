package app;

import components.Settings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

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
        gameMenu.getItems().addAll(analysisBoard);

        getMenus().addAll(fileMenu, gameMenu);
    }
}
