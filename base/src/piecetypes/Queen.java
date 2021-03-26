package piecetypes;

public final class Queen extends Piece {
    private static final char abbr = 'Q';

    Queen(PieceColor color) { super(1, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        return (initialRow == finalRow || initialColumn == finalColumn)
                || (Math.abs(finalRow - initialRow) == Math.abs(finalColumn - initialColumn));
    }
}
