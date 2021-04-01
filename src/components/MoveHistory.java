package components;

import data.Game;
import data.Move;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

import java.util.List;


public class MoveHistory extends FlowPane {
    private class MoveLabel extends Label {
        private final Move move;
        private MoveLabel(Move move) {
            this.move = move;

            setText(move.getNotation());
            getStyleClass().add("move-label");
            addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                if(board.listenForEvents) {
                    MoveLabel clickedLabel = (MoveLabel) mouseEvent.getSource();

                    clickedLabel.select();
                    currentGame.jumpToMove(clickedLabel.move);
                    board.jumpToPosition(clickedLabel.move.getFinalPosition());
                }
            });
        }

        private void select() {
            if(selectedLabel != null) selectedLabel.getStyleClass().remove("selected-move-label");

            selectedLabel = this;
            getStyleClass().add("selected-move-label");
        }
    }

    private final Board board;
    private Game currentGame;

    private MoveLabel selectedLabel;

    public MoveHistory(Board board) {
        this.board = board;

        maxHeightProperty().bind(board.heightProperty());
        prefWidthProperty().bind(heightProperty().multiply(2).divide(3));

        getStyleClass().add("move-history");
        getStylesheets().add("/css/move-history.css");
    }

    Game getCurrentGame() {
        return currentGame;
    }

    public void linkToBoard() {
        currentGame = new Game(board.getCurrentPosition());
    }

    MoveLabel fetchLabel(Move move) {
        for(MoveLabel moveLabel : (List<MoveLabel>)(List<?>)getChildren())
            if(moveLabel.move == move)
                return moveLabel;

        throw new RuntimeException("Could not find your god damn move label!");
    }

    public void addMove(Move move) {
        currentGame.addNewMove(move);
        MoveLabel moveLabel = new MoveLabel(move);

        moveLabel.select();
        getChildren().add(moveLabel);
    }

    public void undoMove() {
        Move lastMove = currentGame.undoMove();
        if(lastMove != null) fetchLabel(lastMove).select();
        else if(selectedLabel != null) {
            selectedLabel.getStyleClass().remove("selected-move-label");
            selectedLabel = null;
        }
    }
    public void undoAll() {
        currentGame.undoAll();
        if(selectedLabel != null) {
            selectedLabel.getStyleClass().remove("selected-move-label");
            selectedLabel = null;
        }
    }

    public void redoMove() {
        Move nextMove = currentGame.redoMove();
        if(nextMove != null) fetchLabel(nextMove).select();
    }
    public void redoAll() {
        Move latestMove = currentGame.redoAll();
        if(latestMove != null) fetchLabel(latestMove).select();
    }
}
