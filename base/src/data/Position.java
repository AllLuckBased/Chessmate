package data;

import logic.FEN;
import piecetypes.Pawn;
import piecetypes.Piece;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Position {
    public class Tile {
        private final char column;
        private final int row;

        private Piece pieceOnTile;

        public Tile(char column, int row) {
            this.column = column;
            this.row = row;
        }

        @Override
        public String toString() {
            return "" + column + row;
        }

        public char getColumn() {
            return column;
        }
        public int getRow() {
            return row;
        }

        public Piece getPieceOnTile() {
            return pieceOnTile;
        }
        public void setPieceOnTile(Piece pieceOnTile) {
            this.pieceOnTile = pieceOnTile;
        }

        public Tile copy() {
            Tile clonedTile = new Tile(column, row);
            clonedTile.setPieceOnTile(pieceOnTile);

            return clonedTile;
        }
    }

    private final List<Tile> tilesOfBoard;
    private final Map<Piece,Tile> piecePlacements;

    boolean verified;

    public Tile EPTile;
    public Piece.PieceColor turn;
    public int halfmoveClock, fullmoveNumber;
    public final boolean[] castlePerms = new boolean[4];

    public Position() {
        verified = true;

        tilesOfBoard = new ArrayList<>(64);
        for(int row=1; row<=8; row++)
            for(char column='a'; column<='h'; column++)
                tilesOfBoard.add(new Tile(column, row));

        piecePlacements = new HashMap<>(32);
        EPTile = null; turn = Piece.PieceColor.WHITE; halfmoveClock=0; fullmoveNumber=1;
        System.arraycopy(new boolean[]{true, true, true, true}, 0, castlePerms, 0, 4);
    }
    public Position(FEN fen) { this(); arrange(fen); }
    public Position(Position prototype) {
        verified = prototype.verified;

        tilesOfBoard = new ArrayList<>();
        prototype.tilesOfBoard.forEach(tile -> tilesOfBoard.add(tile.copy()));

        piecePlacements = new HashMap<>();
        prototype.piecePlacements.forEach((key, value) -> addPiece(key, fetchTile(value.toString())));

        turn = prototype.turn;
        EPTile = prototype.EPTile;
        halfmoveClock = prototype.halfmoveClock;
        fullmoveNumber = prototype.fullmoveNumber;
        System.arraycopy(prototype.castlePerms, 0, castlePerms, 0, 4);
    }

    public List<Tile> getTilesOfBoard() { return tilesOfBoard; }
    public List<Tile> getPromotionOptions(Tile promotionTile) {
        Piece.PieceColor color = promotionTile.row == 8? Piece.PieceColor.WHITE : Piece.PieceColor.BLACK;
        Tile option1 = promotionTile.copy(); option1.setPieceOnTile(Piece.create('Q', color));
        Tile option2 = promotionTile.copy(); option2.setPieceOnTile(Piece.create('R', color));
        Tile option3 = promotionTile.copy(); option3.setPieceOnTile(Piece.create('B', color));
        Tile option4 = promotionTile.copy(); option4.setPieceOnTile(Piece.create('N', color));

        return List.of(option1, option2, option3, option4);
    }

    public Tile fetchTile(int column, int row) {
        if(column<'a' || column>'a'+7 || row<1 || row>8)
            throw new IllegalArgumentException("Invalid tile: " + (char)column + row);
        return tilesOfBoard.get(8*(row-1) + column-'a');
    }
    public Tile fetchTile(String tileName) {
        if(tileName == null) return null;
        if(tileName.length() != 2) throw new RuntimeException("Tile name must be 2 characters long!" + tileName);
        return fetchTile(tileName.charAt(0), Integer.parseInt("" + tileName.charAt(1)));
    }
    public Tile fetchTile(Tile tile) { return fetchTile(tile.column, tile.row); }

    public void addPiece(Piece piece, Tile tile) {
        piecePlacements.remove(tile.pieceOnTile);
        piecePlacements.put(piece, tile);
        tile.setPieceOnTile(piece);
    }
    public void removePiece(Piece piece) {
        piecePlacements.get(piece).setPieceOnTile(null);
        piecePlacements.remove(piece);
    }

    public boolean checkValid() {
        if(isKingChecked(turn.opposite)) return false;

        List<Tile> kingTiles = locatePieces(0);
        if(getPiecesAttackingOn(kingTiles.get(turn.ordinal()), turn.opposite).size() > 2) return false;

        if(kingTiles.get(0) != fetchTile("e1") & (castlePerms[0] || castlePerms[1])) return false;
        if(kingTiles.get(1) != fetchTile("e8") & (castlePerms[2] || castlePerms[3])) return false;

        List<Tile> whiteRookTiles = locatePieces(2, Piece.PieceColor.WHITE);
        List<Tile> blackRookTiles = locatePieces(2, Piece.PieceColor.BLACK);
        if(!whiteRookTiles.contains(fetchTile("h1")) && castlePerms[0])return false;
        if(!whiteRookTiles.contains(fetchTile("a1")) && castlePerms[1])return false;
        if(!blackRookTiles.contains(fetchTile("h8")) && castlePerms[2])return false;
        if(!blackRookTiles.contains(fetchTile("a8")) && castlePerms[3])return false;

        if(EPTile != null) {
            Tile EPfornt = turn == Piece.PieceColor.WHITE ?
                    fetchTile(EPTile.column, EPTile.row - 1) : fetchTile(EPTile.column, EPTile.row + 1);
            Tile EPback = turn == Piece.PieceColor.WHITE ?
                    fetchTile(EPTile.column, EPTile.row + 1) : fetchTile(EPTile.column, EPTile.row - 1);
            if(EPback.pieceOnTile != null || !(EPfornt.pieceOnTile instanceof Pawn)) return false;
        }

        int[][] pieceCount = new int[2][6];
        for(Piece piece : piecePlacements.keySet()) {
            if(piece instanceof Pawn) {
                Pawn pawn = (Pawn) piece;
                if (piecePlacements.get(pawn).row == 1 || piecePlacements.get(pawn).row == 8) return false;
            }
            pieceCount[piece.getColor().ordinal()][piece.getId()]++;
        }

        if(EPTile != null && halfmoveClock!= 0) return false;

        if(pieceCount[0][0] != 1 || pieceCount[1][0] != 1) return false;
        if(Math.max(0, pieceCount[0][1]-1) + Math.max(0, pieceCount[0][2]-2) + Math.max(0, pieceCount[0][3]-2)
                + Math.max(0, pieceCount[0][4]-2) + pieceCount[0][5] > 8) return false;
        return Math.max(0, pieceCount[1][1] - 1) + Math.max(0, pieceCount[1][2] - 2) + Math.max(0, pieceCount[1][3] - 2)
                + Math.max(0, pieceCount[1][4] - 2) + pieceCount[1][5] <= 8;
    }

    public void arrange() { arrange(new FEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")); }
    public void arrange(FEN fen) {
        tilesOfBoard.forEach(tile -> {
            Piece currentPiece = fen.extractNextPiece();
            tile.setPieceOnTile(currentPiece);
            if (currentPiece != null) addPiece(currentPiece, tile);
        });

        turn = fen.extractTurn();
        EPTile = fetchTile(fen.extractEPTileName());
        halfmoveClock = fen.extractHalfmoveClock();
        fullmoveNumber = fen.extractFullmoveNumber();
        System.arraycopy(fen.extractCastlePerms(), 0, castlePerms, 0, 4);
    }

    private List<Position.Tile> getTilesInBetween(Tile end1, Tile end2) {
        if(end1.equals(end2)) throw new RuntimeException("Initial & Final tiles are same: " + end1);
        List<Position.Tile> tilesInBetween = new ArrayList<>();

        int initialRow = end1.getRow(), finalRow = end2.getRow();
        int initialColumn = end1.getColumn(), finalColumn = end2.getColumn();

        boolean horizontal = initialRow == finalRow;
        boolean vertical = initialColumn == finalColumn;
        boolean diagonal = Math.abs(finalRow - initialRow) == Math.abs(finalColumn - initialColumn);

        if(!(horizontal || vertical || diagonal))
            return tilesInBetween;

        int rowIncrement = 0, columnIncrement = 0;
        if(!horizontal) rowIncrement = (finalRow - initialRow) / Math.abs(finalRow - initialRow);
        if (!vertical) columnIncrement = (finalColumn - initialColumn) / Math.abs(finalColumn - initialColumn);

        int tempRow = initialRow + rowIncrement;
        int tempColumn = initialColumn + columnIncrement;

        while (!(tempRow == finalRow && tempColumn == finalColumn)) {
            tilesInBetween.add(fetchTile(tempColumn, tempRow));
            tempRow += rowIncrement; tempColumn += columnIncrement;
        }

        return tilesInBetween;
    }
    List<Piece> getPiecesInBetween(Tile end1, Tile end2) {
        return getTilesInBetween(end1, end2).stream().filter(tile -> tile.pieceOnTile != null)
                .map(Tile::getPieceOnTile).collect(Collectors.toList());
    }

    Tile getPiecePosition(Piece piece) {return piecePlacements.get(piece);}
    private List<Tile> locatePieces(int id, Piece.PieceColor color) {
        return piecePlacements.keySet().stream().filter(piece -> piece.getId() == id && piece.getColor() == color)
                .map(piecePlacements::get).collect(Collectors.toList());
    }
    private List<Tile> locatePieces(Piece.PieceColor color) {
        List<Tile> pieceTiles = new ArrayList<>(16);
        IntStream.range(0, 6).mapToObj(id -> locatePieces(id, color)).forEachOrdered(pieceTiles::addAll);
        return pieceTiles;
    }
    private List<Tile> locatePieces(int id) {
        List<Tile> pieceTiles = new ArrayList<>(16);
        Arrays.stream(Piece.PieceColor.values()).map(color -> locatePieces(id, color)).forEachOrdered(pieceTiles::addAll);
        return pieceTiles;
    }

    public Tile locateKing(Piece.PieceColor kingColor) {
        return locatePieces(0, kingColor).get(0);
    }

    List<Piece> getPiecesAttackingOn(Tile attackedTile, Piece.PieceColor attackingColor) {
        return locatePieces(attackingColor).stream().map(tile -> tile.pieceOnTile)
                .filter(piece -> new Move(piece, attackedTile, this).getMoveType() != Move.MoveType.INVALID)
                .collect(Collectors.toList());
    }
    public boolean isKingChecked(Piece.PieceColor kingColor) {
        return !getPiecesAttackingOn(locatePieces(0, kingColor).get(0), kingColor.opposite).isEmpty();
    }

    public List<Tile> getValidDestinations(Piece piece) {
        Move move = new Move(piece, this);
        if(move.getMoveType() == Move.MoveType.INVALID)
            return new ArrayList<>();

        List<Tile> list = new ArrayList<>();
        for (Tile tile : tilesOfBoard) {
            if (move.setFinalTile(tile).getMoveType() != Move.MoveType.INVALID) {
                list.add(tile);
            }
        }
        return list;
    }
    public List<Piece> getPiecesThatCanMove() {
        return piecePlacements.keySet().stream().filter(piece -> !getValidDestinations(piece).isEmpty())
                .collect(Collectors.toList());
    }
}
