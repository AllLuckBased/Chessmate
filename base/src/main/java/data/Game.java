package data;

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

    public void addNewMove(Move move) {
        move.setMoveNumber(getCurrentPosition().fullmoveNumber);
        moveStack.push(move);
    }

    public Move undoMove() {
        if(moveStack.isEmpty()) return null;
        reverseMoveStack.push(moveStack.pop());
        return !moveStack.isEmpty()? moveStack.peek() : null;
    }
    public void undoAll() {
        while(!moveStack.isEmpty()) {
            reverseMoveStack.push(moveStack.pop());
        }
    }

    public Move redoMove() {
        if(reverseMoveStack.isEmpty()) return null;
        moveStack.push(reverseMoveStack.pop());
        return !moveStack.isEmpty()? moveStack.peek() : null;
    }
    public Move redoAll() {
        while(!reverseMoveStack.isEmpty()) {
            moveStack.push(reverseMoveStack.pop());
        }
        return !moveStack.isEmpty()? moveStack.peek() : null;
    }

    public void jumpToMove(Move move) {
        if(move.getMoveNumber() < getCurrentPosition().fullmoveNumber)
            while((!moveStack.isEmpty()? moveStack.peek() : null) != move)
                reverseMoveStack.push(moveStack.pop());
        else while((!moveStack.isEmpty()? moveStack.peek() : null) != move)
            moveStack.push(reverseMoveStack.pop());
    }
}
