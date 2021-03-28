package data;

import piecetypes.King;
import piecetypes.Pawn;
import piecetypes.Piece;
import piecetypes.Rook;

import java.util.ArrayList;
import java.util.List;

public class Move {
    public enum MoveType { INVALID, NORMAL, CASTLE, EN_PASSANT, PROMOTION }

    private final Piece pieceToMove;
    private final Position initialPosition;
    private final Piece.PieceColor pieceColor;
    private final Position.Tile initialTile;

    private MoveType moveType;
    private Position.Tile finalTile;
    private Piece promotedPiece;
    private Position finalPosition;

    private int moveNumber;

    Move(Piece pieceToMove, Position.Tile destination, Position initialPosition) {
        this(pieceToMove, initialPosition);
        setFinalTile(destination);
    }
    public Move(Piece pieceToMove, Position initialPosition) {
        if(pieceToMove == null || initialPosition == null)
            throw new RuntimeException("Cannot instantiate move object without a piece & position");

        this.pieceToMove = pieceToMove;
        this.initialPosition = initialPosition;

        pieceColor = pieceToMove.getColor();
        initialTile = initialPosition.getPiecePosition(pieceToMove);

        if(pieceColor != initialPosition.turn)
            moveType = MoveType.INVALID;
    }
    public Move setFinalTile(Position.Tile finalTile) {
        this.finalTile = finalTile; moveType = null;
        if(initialTile.equals(finalTile)) moveType = MoveType.INVALID;
        else {
            Piece opponentPiece = finalTile.getPieceOnTile();
            if(!pieceToMove.possibleMove(initialTile, finalTile) ||
                    (opponentPiece != null && opponentPiece.getColor() == pieceColor) ||
                    !initialPosition.getPiecesInBetween(initialTile, finalTile).isEmpty())
                moveType = MoveType.INVALID;
            else {
                int initialColumn = initialTile.getColumn(), finalColumn = finalTile.getColumn(), finalRow = finalTile.getRow();
                if (pieceToMove instanceof Pawn) {
                    if (Math.abs(finalColumn - initialColumn) == 0 && opponentPiece != null) moveType = MoveType.INVALID;
                    else if (Math.abs(finalColumn - initialColumn) == 1 && opponentPiece == null) {
                        if (!finalTile.equals(initialPosition.EPTile)) moveType = MoveType.INVALID;
                        else moveType = MoveType.EN_PASSANT;
                    }
                    else if (finalRow == 8 || finalRow == 1) moveType = MoveType.PROMOTION;
                } else if (pieceToMove instanceof King && Math.abs(finalColumn - initialColumn) == 2) {
                    if (!initialPosition.getPiecesInBetween(initialTile, initialPosition.fetchTile(
                            finalColumn == 'g' ? 'h' : 'a', finalRow)).isEmpty()) moveType = MoveType.INVALID;
                    else {
                        boolean kingChecked = initialPosition.isKingChecked(pieceColor);
                        boolean intermediateTileIsAttacked = !initialPosition.getPiecesAttackingOn(initialPosition.fetchTile(
                                finalColumn == 'g' ? 'f' : 'd', finalRow), pieceColor.opposite).isEmpty();

                        if (kingChecked || intermediateTileIsAttacked) moveType = MoveType.INVALID;
                        else {
                            boolean correctCastlingMove =
                                    (finalColumn - initialColumn == 2 && pieceColor == Piece.PieceColor.WHITE && initialPosition.castlePerms[0]) ||
                                            (finalColumn - initialColumn == -2 && pieceColor == Piece.PieceColor.WHITE && initialPosition.castlePerms[1]) ||
                                            (finalColumn - initialColumn == 2 && pieceColor == Piece.PieceColor.BLACK && initialPosition.castlePerms[2]) ||
                                            (finalColumn - initialColumn == -2 && pieceColor == Piece.PieceColor.BLACK && initialPosition.castlePerms[3]);

                            if (!correctCastlingMove) moveType = MoveType.INVALID;
                            else moveType = MoveType.CASTLE;
                        }
                    }
                }
                if(moveType == null) moveType = MoveType.NORMAL;
                if (initialPosition.verified) {
                    Position tempPosition = execute(moveType);
                    if (tempPosition.isKingChecked(pieceColor)) moveType = MoveType.INVALID;
                    finalPosition = tempPosition;
                    finalPosition.verified = true;
                }
            }
        }
        return this;
    }
    private Position execute(MoveType moveType) {
        Position finalPosition = new Position(initialPosition);
        finalPosition.verified = false; finalPosition.EPTile = null;

        Position.Tile initialTile = finalPosition.fetchTile(this.initialTile);
        Position.Tile finalTile = finalPosition.fetchTile(this.finalTile);

        switch (moveType) {
            case NORMAL, PROMOTION -> {
                finalPosition.removePiece(pieceToMove);
                finalPosition.addPiece(pieceToMove, finalTile);
            }
            case EN_PASSANT -> {
                finalPosition.removePiece(pieceToMove);
                finalPosition.addPiece(pieceToMove, finalTile);
                Position.Tile enPassantTile = finalPosition.fetchTile(finalTile.getColumn(), pieceColor == Piece.PieceColor.WHITE ?
                        finalTile.getRow() - 1 : finalTile.getRow() + 1);
                finalPosition.removePiece(enPassantTile.getPieceOnTile());
            }
            case CASTLE -> {
                finalPosition.removePiece(pieceToMove);
                finalPosition.addPiece(pieceToMove, finalTile);
                Position.Tile initialRookTile = finalPosition.fetchTile(finalTile.getColumn() == 'g' ? 'h' : 'a', finalTile.getRow());
                Position.Tile finalRookTile = finalPosition.fetchTile(finalTile.getColumn() == 'g' ? 'f' : 'd', finalTile.getRow());
                Piece associatedRook = initialRookTile.getPieceOnTile();
                finalPosition.removePiece(associatedRook);
                finalPosition.addPiece(associatedRook, finalRookTile);
            }
        }

        if(pieceToMove instanceof Pawn && Math.abs(finalTile.getRow() - initialTile.getRow()) == 2)
                finalPosition.EPTile = pieceColor == Piece.PieceColor.WHITE ?
                        finalPosition.fetchTile(initialTile.getColumn(), initialTile.getRow() + 1) :
                        finalPosition.fetchTile(initialTile.getColumn(), initialTile.getRow() - 1);

        else if (pieceToMove instanceof King) {
            if (pieceColor == Piece.PieceColor.WHITE) {
                finalPosition.castlePerms[0] = false;
                finalPosition.castlePerms[1] = false;
            }
            else {
                finalPosition.castlePerms[2] = false;
                finalPosition.castlePerms[3] = false;
            }
        }
        else if (pieceToMove instanceof Rook) {
            if (initialTile.equals(initialPosition.fetchTile('h', 1))) finalPosition.castlePerms[0] = false;
            else if (initialTile.equals(initialPosition.fetchTile('a', 1))) finalPosition.castlePerms[1] = false;
            else if (initialTile.equals(initialPosition.fetchTile('h', 8))) finalPosition.castlePerms[2] = false;
            else if (initialTile.equals(initialPosition.fetchTile('a', 8))) finalPosition.castlePerms[3] = false;
        }

        finalPosition.turn = pieceColor.opposite;
        return finalPosition;
    }
    public void setPromotedPiece(Piece piece) {
        finalPosition.addPiece(piece, finalPosition.fetchTile(finalTile));
        promotedPiece = piece;
    }

    public MoveType getMoveType() { return moveType; }
    public Position getFinalPosition() { return finalPosition; }

    int getMoveNumber() { return moveNumber; }
    void setMoveNumber(int moveNumber) { this.moveNumber = moveNumber; }

    private boolean notationGenerationPossible() {
        if(moveType == null || moveType == MoveType.INVALID) return false;
        if(moveType == MoveType.PROMOTION && promotedPiece == null) return false;
        return moveNumber != 0;
    }
    public String getNotation() {
        if(!notationGenerationPossible())
            throw new RuntimeException("Cannot generate notation as complete info absent!");

        StringBuilder moveNotation = new StringBuilder(9);
        if(pieceColor == Piece.PieceColor.WHITE) moveNotation.append("" + moveNumber + ".");

        if (moveType == Move.MoveType.CASTLE)
            return finalTile.getColumn() == 'g' ? moveNotation.append("O-O").toString() : moveNotation.append("O-O-O").toString();

        if(!(pieceToMove instanceof Pawn)) {
            moveNotation.append(Character.toUpperCase(pieceToMove.getAbbr()));

            List<Piece> controlsFinalTile = initialPosition.getPiecesAttackingOn(finalTile, pieceToMove.getColor());
            boolean ambiguity = false, columnAmbiguity = false, rowAmbiguity = false;
            for(Piece friendlyPiece : controlsFinalTile) {
                if(friendlyPiece.equals(pieceToMove)) continue;
                if(friendlyPiece instanceof Pawn || friendlyPiece.getClass() != pieceToMove.getClass()) continue;

                ambiguity = true;
                Position.Tile friendlyPiecePosition = initialPosition.getPiecePosition(friendlyPiece);
                if(friendlyPiecePosition.getColumn() == initialTile.getColumn()) columnAmbiguity = true;
            }
            if(columnAmbiguity) {
                for(Piece friendlyPiece : controlsFinalTile) {
                    if(friendlyPiece.equals(pieceToMove)) continue;
                    if(friendlyPiece instanceof Pawn || friendlyPiece.getClass() != pieceToMove.getClass()) continue;

                    Position.Tile friendlyPiecePosition = initialPosition.getPiecePosition(friendlyPiece);
                    if(friendlyPiecePosition.getRow() == initialTile.getRow()) rowAmbiguity = true;
                }
            }

            if(ambiguity) {
                if(!columnAmbiguity) moveNotation.append(initialTile.getColumn());
                else if(!rowAmbiguity) moveNotation.append(initialTile.getRow());
                else moveNotation.append(initialTile.toString());
            }
        }

        if(finalTile.getPieceOnTile() != null || moveType == Move.MoveType.EN_PASSANT) {
            if(pieceToMove instanceof Pawn) moveNotation.append(initialTile.getColumn());
            moveNotation.append("x");
        }

        moveNotation.append(finalTile.toString());

        if(moveType == Move.MoveType.PROMOTION)
            moveNotation.append("=").append(Character.toUpperCase(promotedPiece.getAbbr()));

        if(finalPosition.isKingChecked(pieceToMove.getColor().opposite))
            moveNotation.append(finalPosition.getPiecesThatCanMove().isEmpty() ? "#" : "+");

        return moveNotation.toString();
    }
}