package piecetypes;

public final class Bishop extends Piece {
    private static final char abbr = 'B';

    Bishop(PieceColor color) { super(3, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        return Math.abs(finalRow - initialRow) == Math.abs(finalColumn - initialColumn);
    }
}
