package piecetypes;

import java.util.Arrays;

public final class King extends Piece {
    private static final char abbr = 'K';

    King(PieceColor color) { super(0, color); }

    @Override
    public char getAbbr() {
        return color == PieceColor.WHITE ? Character.toUpperCase(abbr) : Character.toLowerCase(abbr);
    }

    @Override
    public boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow) {
        Integer[] adjValues = {-1, 0, 1};
        boolean normalMove = (Arrays.asList(adjValues).contains(finalRow - initialRow)
                && Arrays.asList(adjValues).contains(finalColumn - initialColumn));

        boolean castleMove = initialColumn=='e' && (getColor() == PieceColor.WHITE? initialRow == 1: initialRow == 8)
                && Math.abs(finalColumn - initialColumn) == 2 && finalRow==initialRow;
        return normalMove || castleMove;
    }
}