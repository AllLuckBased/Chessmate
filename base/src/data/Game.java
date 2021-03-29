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

    public Move undoMove() {
        if(moveStack.isEmpty()) return null;
        reverseMoveStack.push(moveStack.pop());
        return moveStack.peek();
    }
    public void undoAll() {
        while(!moveStack.isEmpty()) {
            reverseMoveStack.push(moveStack.pop());
        }
    }

    public Move redoMove() {
        if(reverseMoveStack.isEmpty()) return null;
        Move nextMove = reverseMoveStack.pop();
        moveStack.push(nextMove);
        return nextMove;
    }
    public Move redoAll() {
        while(!reverseMoveStack.isEmpty()) {
            moveStack.push(reverseMoveStack.pop());
        }
        return moveStack.peek();
    }
}
