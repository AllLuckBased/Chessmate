package components;

import data.Game;
import data.Move;
import data.Position;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import piecetypes.Piece;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

public class Board extends StackPane {
    // cached images...
    private static Image[][] pieceset = new Image[2][6];

    private class Tile extends StackPane {
        private data.Position.Tile model;
        private final Pane bgLayer, fgLayer;
        private final ImageView pieceView;

        private Tile(data.Position.Tile model) {
            this.model = model;

            bgLayer = new Pane();
            bgLayer.prefHeightProperty().bindBidirectional(prefHeightProperty());
            bgLayer.prefWidthProperty().bindBidirectional(prefWidthProperty());

            pieceView = new ImageView();
            pieceView.fitHeightProperty().bindBidirectional(prefHeightProperty());
            pieceView.fitWidthProperty().bindBidirectional(prefWidthProperty());

            fgLayer = new Pane();
            fgLayer.prefHeightProperty().bindBidirectional(prefHeightProperty());
            fgLayer.prefWidthProperty().bindBidirectional(prefWidthProperty());

            // There are 8 tiles in every row/column so divide by 8, and border in each tile is 3px thick so subtract 6.
            prefHeightProperty().bind(Board.this.heightProperty().divide(8));
            prefWidthProperty().bind(Board.this.widthProperty().divide(8));

            getChildren().addAll(bgLayer, pieceView, fgLayer);
            getStyleClass().add("tile");

            refresh();
        }

        private void select() {
            if (selectedTile != null) selectedTile.deselect();
            if(model.getPieceOnTile() != null) {
                selectedTile = this;
                setId("selected-tile");

                List<data.Position.Tile> moveDest = position.model.getValidDestinations(model.getPieceOnTile());
                for (data.Position.Tile tileModel : moveDest) {
                    Tile tile = fetchTile(tileModel);
                    tile.fgLayer.getStyleClass().add("move-dest");
                    if (tileModel.getPieceOnTile() != null)
                        tile.fgLayer.getStyleClass().add("capture");
                }
            }
        }
        private void deselect() {
            if(this == selectedTile && selectedTile.model.getPieceOnTile() != null) {
                List<data.Position.Tile> moveDest = position.model.getValidDestinations(model.getPieceOnTile());
                for (data.Position.Tile tile : moveDest)
                    fetchTile(tile).fgLayer.getStyleClass().clear();

                selectedTile = null;
                setId(null);
            }
        }

        private void refresh() {
            Piece pieceOnTile = model.getPieceOnTile();
            if(pieceOnTile == null) pieceView.setImage(null);
            else if(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()] != null)
                pieceView.setImage(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()]);
            else {
                try {
                    pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()] =
                            new Image(model.getPieceOnTile().getIconPath().openStream());
                    pieceView.setImage(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()]);
                } catch (IOException e) { throw new RuntimeException("Exception occured while reading piece icons"); }
            }

            bgLayer.getStyleClass().clear();
            getStyleClass().clear();
            fgLayer.getStyleClass().clear();

        }
        private void update(data.Position.Tile model) {
            this.model = model;
            refresh();
        }
    }
    private class Position extends GridPane {
        private data.Position model;

        private Position(data.Position model) {
            this.model = model;
            Iterator<data.Position.Tile> tileIterator = model.getTilesOfBoard().listIterator();
            for(int row = 0; row<8; row++)
                for(int column=0; column<8; column++)
                    add(new Board.Tile(tileIterator.next()), column, 8-row);

            prefWidthProperty().bind(Board.this.widthProperty());
            prefHeightProperty().bind(Board.this.heightProperty());

            getStyleClass().add("position");
        }

        private void activate() {
            for (Node tile : getChildren()) tile.addEventHandler(MouseEvent.MOUSE_CLICKED, moveHandler);
        }
        private void deactivate() {
            for (Node tile : getChildren()) tile.removeEventHandler(MouseEvent.MOUSE_CLICKED, moveHandler);
        }

        private void refresh() {
            ((List<Board.Tile>) (List<?>) getChildren()).forEach(Board.Tile::refresh);
        }
        private void update(data.Position model) {
            this.model = model;

            Iterator<data.Position.Tile> tileIterator = model.getTilesOfBoard().listIterator();
            for(Board.Tile tile : (List<Board.Tile>)(List<?>)getChildren()) tile.update(tileIterator.next());

            if(model.isKingChecked(model.turn))
                fetchTile(model.locateKing(model.turn)).bgLayer.getStyleClass().add("check");

            if(selectedTile != null) {
                selectedTile.setId(null);
                selectedTile = null;
            }
        }
    }
    private class PromotionOptions extends GridPane {
        private Pane promotionVeil;
        private PromotionOptions(Tile finalTile) {
            promotionVeil = new Pane();
            promotionVeil.getStyleClass().add("promotion-veil");
            Board.this.getChildren().add(promotionVeil);

            int rowIndex = 0;
            for(data.Position.Tile optionModel : position.model.getPromotionOptions(finalTile.model)) {
                Tile option = new Tile(optionModel);
                option.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                    Board.this.getChildren().remove(PromotionOptions.this);
                    Board.this.getChildren().remove(promotionVeil);

                    Tile clickedTile = (Tile) mouseEvent.getSource();
                    moveToMake.setPromotedPiece(clickedTile.model.getPieceOnTile());

                    currentGame.addNewMove(moveToMake);
                    update(moveToMake.getFinalPosition());
                });
                add(option, 0, rowIndex++);
            }

            setGridLinesVisible(true);
            setMaxSize(Board.this.getWidth()/8, Board.this.getHeight()/2);

            getStyleClass().add("promotion-options");
        }
    }

    private final ImageView background;
    private Position position = new Position(new data.Position());

    private MoveHistory moveHistory; private Game currentGame;

    private Tile selectedTile; Move moveToMake;
    private final EventHandler<MouseEvent> moveHandler = mouseEvent -> {
        Tile clickedTile = (Tile) mouseEvent.getSource();
        data.Position.Tile clickedTileModel = clickedTile.model;

        Move temp;
        Piece pieceToMove = clickedTileModel.getPieceOnTile();
        data.Position currentPosition = currentGame.getCurrentPosition();

        if(moveToMake == null) {
            if(pieceToMove != null) {
                temp = new Move(pieceToMove, currentPosition);
                if (temp.getMoveType() != Move.MoveType.INVALID) {
                    moveToMake = temp;
                    clickedTile.select();
                }
            }
            return;
        }

        moveToMake.setFinalTile(clickedTileModel);
        if(moveToMake.getMoveType() == Move.MoveType.INVALID) {
            if (!clickedTile.equals(selectedTile) && pieceToMove != null &&
                    (temp = new Move(pieceToMove, currentPosition)).getMoveType() != Move.MoveType.INVALID) {
                moveToMake = temp;
                clickedTile.select();
            }
            else {
                moveToMake = null;
                selectedTile.deselect();
            }
        } else {
            if(moveToMake.getMoveType() == Move.MoveType.PROMOTION)
                getChildren().add(new PromotionOptions(clickedTile));
            else {
                currentGame.addNewMove(moveToMake);
                moveHistory.addMove(moveToMake);
                update(moveToMake.getFinalPosition());
                if(position.model.getPiecesThatCanMove().isEmpty()) position.deactivate();
            }
        }
    };
    private final EventHandler<KeyEvent> arrowKeyPress = keyEvent -> {
        switch (keyEvent.getCode()) {
            case LEFT: currentGame.undoMove(); break;
            case RIGHT: currentGame.redoMove(); break;
            case UP: currentGame.undoAll(); break;
            case DOWN: currentGame.redoAll(); break;
            default:
        }
        update(currentGame.getCurrentPosition());
    };


    public Board() {
        background = new ImageView( new Image("boardthemes/" + Preferences.userRoot().get("Board Theme", "Green") + ".png",
                        560, 560, true, true));
        background.fitWidthProperty().bindBidirectional(position.prefWidthProperty());
        background.fitHeightProperty().bindBidirectional(position.prefHeightProperty());

        setMinSize(240, 240);
        getChildren().addAll(background, position);
        getStyleClass().add("board");
    }
    @Override
    public void resize(double width, double height) {
        //System.out.println("Resize method was called with params(W,H): " + width + ", " + height);
        double size = Math.min(width, height);
        super.resize(size, size);
    }

    public Tile fetchTile(data.Position.Tile tileModel) {
        for(Tile tile : (List<Tile>)(List<?>)position.getChildren())
            if(tile.model == tileModel) return tile;
        throw new RuntimeException("Could not find your god damn tile.");
    }

    public void refresh() {
        pieceset = new Image[2][6];
        background.setImage( new Image("boardthemes/" + Preferences.userRoot().get("Board Theme", "Green") + ".png",
                560, 560, true, true));
        position.refresh();
    }
    public void update(data.Position newPosition) {
        position.update(newPosition);
        moveToMake = null;
    }

    public void startGame(MoveHistory moveHistory) {
        if(currentGame != null) {
            moveToMake = null; selectedTile = null;
            getChildren().remove(0, getChildren().size());
            position = new Position(new data.Position());
            getChildren().addAll(background, position);
        }

        currentGame = new Game(position.model);
        position.model.arrange(); refresh();

        this.moveHistory = moveHistory;

        position.activate();
        setFocusTraversable(true);
        addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyPress);
    }
}