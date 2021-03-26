package piecetypes;

import data.Position;

import java.net.URL;
import java.util.prefs.Preferences;

public abstract class Piece {
    public static Piece create(char abbr, PieceColor requiredColor) {
        return switch (Character.toUpperCase(abbr)) {
            case 'K' -> new King(requiredColor);
            case 'Q' -> new Queen(requiredColor);
            case 'R' -> new Rook(requiredColor);
            case 'B' -> new Bishop(requiredColor);
            case 'N' -> new Knight(requiredColor);
            case 'P' -> new Pawn(requiredColor);
            default -> throw new RuntimeException("Could not recognize given piece abbreviation: " + abbr);
        };
    }
    public static Piece create(char abbr) {
        return create(abbr, Character.isUpperCase(abbr) ? PieceColor.WHITE : PieceColor.BLACK);
    }

    public enum PieceColor {
        WHITE, BLACK;
        static {
            WHITE.abbr = 'W'; WHITE.opposite = BLACK;
            BLACK.abbr = 'B'; BLACK.opposite = WHITE;
        }

        public char abbr;
        public PieceColor opposite;
    }

    private final int id;
    final PieceColor color;

    Piece(int id, PieceColor color) {
        this.id = id;
        this.color = color;
    }

    public int getId() { return id; }
    public PieceColor getColor() { return color; }

    public URL getIconPath() {
        return getClass().getResource("/../Chessmate/piecethemes/" + Preferences.userRoot().get("Piece Theme", "Neo") +
                "/" + color.abbr + getAbbr() + ".png");
    }

    public boolean possibleMove(Position.Tile initialTile, Position.Tile finalTile) {
        return possibleMove(initialTile.getColumn(), initialTile.getRow(), finalTile.getColumn(), finalTile.getRow());
    }

    public abstract char getAbbr();
    abstract boolean possibleMove(int initialColumn, int initialRow, int finalColumn, int finalRow);
}
