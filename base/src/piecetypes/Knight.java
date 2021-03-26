package piecetypes;

public final class Knight extends Piece {
    private static final char abbr = 'N';

    Knight(PieceColor color) { super(4, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        return (Math.abs(finalRow - initialRow) == 2 && Math.abs(finalColumn - initialColumn) == 1)
                || (Math.abs(finalColumn - initialColumn) == 2 && Math.abs(finalRow - initialRow) == 1);
    }
}
