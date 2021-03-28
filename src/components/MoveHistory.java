package components;

import data.Move;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class MoveHistory extends FlowPane {
    private final Board board;

    public MoveHistory(Board board) {
        this.board = board;
        setHgap(3);

        maxHeightProperty().bind(board.heightProperty());
        prefWidthProperty().bind(heightProperty().divide(2));

        setMaxWidth(300);

        getStyleClass().add("move-history");
    }

    public void addMove(Move move) {
        getChildren().add(new Label(move.getNotation()));
    }
}
