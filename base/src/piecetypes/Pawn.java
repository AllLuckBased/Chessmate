package piecetypes;

public final class Pawn extends Piece {
    private static final char abbr = 'P';

    Pawn(PieceColor color) { super(5, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        boolean correctDirection = (color == PieceColor.WHITE && finalRow - initialRow > 0) ||
                (color == PieceColor.BLACK && finalRow - initialRow < 0);

        boolean hasMoved = color == PieceColor.WHITE? initialRow != 2 : initialRow != 7;

        boolean normalMove = correctDirection && Math.abs(finalRow - initialRow) == 1 && finalColumn == initialColumn;
        boolean doubleMove = !hasMoved && correctDirection && Math.abs(finalRow - initialRow) == 2 && finalColumn == initialColumn;
        boolean capture = correctDirection && Math.abs(finalRow - initialRow) == 1 && Math.abs(finalColumn - initialColumn) == 1;

        return normalMove || doubleMove || capture;
    }
}
