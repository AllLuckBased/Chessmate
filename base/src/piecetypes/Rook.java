package piecetypes;

public final class Rook extends Piece {
    private static final char abbr = 'R';

    Rook(PieceColor color) { super(2, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        return initialRow == finalRow || initialColumn == finalColumn;
    }
}
