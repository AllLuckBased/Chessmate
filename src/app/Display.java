package app;

import components.Board;
import components.MoveHistory;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        HBox.setHgrow(board, Priority.ALWAYS);

        HBox center = new HBox();
        center.setAlignment(Pos.CENTER);

        MoveHistory moveHistory = new MoveHistory(board);
        center.getChildren().addAll(board, moveHistory);

        setCenter(center);
        getStylesheets().add("/css/analysis-board.css");

        board.startGame(moveHistory);
    }
}
