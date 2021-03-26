package logic;

import data.Position;
import piecetypes.Piece;

public class FEN {
    private final String fen;
    private String[] rowData;
    private int rowIndex = 0, parseIndex = 0, emptyTileTracker = 0;

    public FEN(String fen) {
        this.fen = fen;
        rowData = fen.split(" ")[0].split("/");
    }
    public FEN(Position position) {
        StringBuilder fen = new StringBuilder(65);

        int tileNumber = 0; int spaceCounter = 0;
        while(tileNumber < position.getTilesOfBoard().size()) {
            Position.Tile tile = position.getTilesOfBoard().get(tileNumber);
            if (tileNumber%8 == 0 && tileNumber!=0) {
                if(spaceCounter != 0) fen.append(spaceCounter);
                fen.append("/");
                spaceCounter = 0;
            }
            tileNumber++;
            if(tile.getPieceOnTile() == null) spaceCounter++;
            else {
                if (spaceCounter != 0) fen.append(spaceCounter);
                fen.append(tile.getPieceOnTile().getAbbr());
                spaceCounter = 0;
            }
        }

        fen.append(" ").append(position.turn.abbr).append(" ");

        StringBuilder castlePermms = new StringBuilder(4);
        for (int i = 0; i < position.castlePerms.length; i++) {
            if (!position.castlePerms[i]) continue;
            switch (i) {
                case 0 -> castlePermms.append('K');
                case 1 -> castlePermms.append('Q');
                case 2 -> castlePermms.append('k');
                case 3 -> castlePermms.append('q');
            }
        }
        if(castlePermms.isEmpty()) castlePermms.append("-");
        fen.append(castlePermms);

        if(position.EPTile != null)
            fen.append(" ").append(position.EPTile).append(" ");
        else fen.append(" - ");

        fen.append(position.halfmoveClock);
        fen.append(" ").append(position.fullmoveNumber);

        this.fen = fen.toString();
    }

    public Piece extractNextPiece() {
        if(!(emptyTileTracker-- <= 0)) return null;
        if(!(parseIndex < rowData[7 - rowIndex].length())) {
            parseIndex = 0; rowIndex++;
            return extractNextPiece();
        }
        if(Character.isLetter(rowData[7-rowIndex].charAt(parseIndex))) return Piece.create(rowData[7-rowIndex].charAt(parseIndex++));
        if(Character.isDigit(rowData[7-rowIndex].charAt(parseIndex))) {
            emptyTileTracker = Integer.parseInt("" + rowData[7-rowIndex].charAt(parseIndex++));
            emptyTileTracker--;
            return null;
        }
        throw new RuntimeException("Unhandled case: FEN Character = " + fen.charAt(parseIndex));
    }

    public Piece.PieceColor extractTurn() {
        return fen.split(" ")[1].charAt(0) == 'w' ? Piece.PieceColor.WHITE : Piece.PieceColor.BLACK;
    }
    public boolean[] extractCastlePerms() {
        boolean[] castlePerms = new boolean[] {false, false, false, false};
        for(char castlePerm : fen.split(" ")[2].toCharArray()) {
            switch (castlePerm) {
                case 'K' -> castlePerms[0] = true;
                case 'Q' -> castlePerms[1] = true;
                case 'k' -> castlePerms[2] = true;
                case 'q' -> castlePerms[3] = true;
            }
        }
        return castlePerms;
    }
    public String extractEPTileName() {
        return fen.split(" ")[3].equals("-") ? null : fen.split(" ")[3];
    }
    public int extractHalfmoveClock() {
        return Integer.parseInt(fen.split(" ")[4]);
    }
    public int extractFullmoveNumber() {
        return Integer.parseInt(fen.split(" ")[5]);
    }
}
