package app;

import components.Board;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Display extends BorderPane {
    Board board;

    public Display() {
        Toolbar toolbar = new Toolbar(this);
        toolbar.prefWidthProperty().bind(widthProperty());
        setTop(toolbar);

        board = new Board();
        getStyleClass().add("display");
    }

    public Board getBoard() {
        return board;
    }

    void showHomeScreen() {
        HBox displayCenter = new HBox(board);
        displayCenter.setAlignment(Pos.CENTER);
        displayCenter.setSpacing(10);

        setCenter(displayCenter);
    }
    void showAnalysisBoard() {
        setCenter(board);
        getStylesheets().add("/css/analysis-board.css");

        board.startGame();
    }
}
