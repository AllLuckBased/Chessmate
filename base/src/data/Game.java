package data;

import piecetypes.Piece;

import java.util.Stack;

public class Game {
    private final Position startingPosition;
    private final Stack<Move> moveStack, reverseMoveStack;

    public Game(Position startingPosition) {
        this.startingPosition = startingPosition;

        moveStack = new Stack<>();
        reverseMoveStack = new Stack<>();
    }

    public Position getCurrentPosition() {
        if(moveStack.empty()) return startingPosition;
        return moveStack.peek().getFinalPosition();
    }

    private int getCurrentMoveNumber() {
        if(moveStack.empty()) return startingPosition.fullmoveNumber;
        else return getCurrentPosition().turn == Piece.PieceColor.WHITE ?
                moveStack.peek().getMoveNumber() + 1 : moveStack.peek().getMoveNumber();
    }
    public void addNewMove(Move move) {
        move.setMoveNumber(getCurrentMoveNumber());
        moveStack.push(move);
    }

    public void undoMove() {
        if(moveStack.isEmpty()) return;
        Move lastMove = moveStack.pop();
        reverseMoveStack.push(lastMove);
    }
    public void undoAll() {
        while(!moveStack.isEmpty()) {
            reverseMoveStack.push(moveStack.pop());
        }
    }
    public void redoMove() {
        if(reverseMoveStack.isEmpty()) return;
        Move nextMove = reverseMoveStack.pop();
        moveStack.push(nextMove);
    }
    public void redoAll() {
        while(!reverseMoveStack.isEmpty()) {
            moveStack.push(reverseMoveStack.pop());
        }
    }
}
