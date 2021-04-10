package app;

import components.Board;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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
        setCenter(board);
    }

    void showAnalysisBoard() {
        HBox center = new HBox();
        center.setSpacing(20);
        center.setAlignment(Pos.CENTER);
        HBox.setHgrow(board, Priority.ALWAYS);
        center.setPadding(new Insets(20));

        center.getChildren().addAll(board, board.startGame());
        setCenter(center);

        getStylesheets().add("/css/analysis-board.css");
    }
}
