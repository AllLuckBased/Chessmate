package components;

import data.Move;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
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

    private final class DragData {
        private final Tile dragSource;
        private final double initialTranslateX, initialTranslateY, initialMouseX, initialMouseY;

        private final ImageView draggedPieceView;

        public DragData(Tile dragSource, MouseEvent dragEvent) {
            if(permSelectedTile != null)
                permSelectedTile.deselect();

            dragSource.select();
            this.dragSource = dragSource;

            initialTranslateX = dragSource.getLayoutX() - getWidth()/2 + dragSource.getWidth()/2;
            initialTranslateY = dragSource.getLayoutY() - getHeight()/2 + dragSource.getHeight()/2;
            initialMouseX = dragEvent.getScreenX(); initialMouseY = dragEvent.getScreenY();

            draggedPieceView = new ImageView(dragSource.pieceView.getImage());
            draggedPieceView.setFitHeight(dragSource.pieceView.getFitHeight());
            draggedPieceView.setFitWidth(dragSource.pieceView.getFitWidth());
            draggedPieceView.setTranslateX(initialTranslateX);
            draggedPieceView.setTranslateY(initialTranslateY);

            Board.this.getChildren().add(draggedPieceView);
            dragSource.pieceView.setImage(null);
        }
    }

    boolean listenForEvents = true;
    private DragData dragData = null;
    private Tile tempSelectedTile, permSelectedTile; Move moveToMake;

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
            if(model.getPieceOnTile() != null) {
                tempSelectedTile = this;
                setId("selected-tile");

                List<data.Position.Tile> moveDest = position.model.getValidDestinations(model.getPieceOnTile());
                for (data.Position.Tile tileModel : moveDest) {
                    Tile tile = fetchTile(tileModel);
                    tile.fgLayer.getStyleClass().add("move-dest");
                    if (tileModel.getPieceOnTile() != null)
                        tile.fgLayer.getStyleClass().add("capture");
                }

                moveToMake = new Move(model.getPieceOnTile(), moveHistory.getCurrentGame().getCurrentPosition());
            }
        }
        private void deselect() {
            if(model.getPieceOnTile() != null) {
                List<data.Position.Tile> moveDest = position.model.getValidDestinations(model.getPieceOnTile());
                for (data.Position.Tile tile : moveDest)
                    fetchTile(tile).fgLayer.getStyleClass().clear();

                tempSelectedTile = null;
                setId(null);
            }
        }

        private void refresh() {
            Piece pieceOnTile = model.getPieceOnTile();
            if(pieceOnTile == null) pieceView.setImage(null);
            else if(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()] != null)
                pieceView.setImage(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()]);
            else {
                pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()] =
                        new Image("piecethemes/" + model.getPieceOnTile().getIconPath());
                pieceView.setImage(pieceset[pieceOnTile.getColor().ordinal()][pieceOnTile.getId()]);
            }
        }

        private void update(data.Position.Tile model) {
            this.model = model;
            refresh();
            fgLayer.getStyleClass().clear();
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
            for (Node node : getChildren()) {
                Tile tile = (Tile) node;
                tile.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressEvent -> {
                    if(mousePressEvent.getButton() == MouseButton.SECONDARY) return;
                    handleMove((Tile)mousePressEvent.getSource(), mousePressEvent);
                    mousePressEvent.consume();
                });
                tile.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDragEvent -> {
                    if(!listenForEvents || dragData == null || mouseDragEvent.getButton() == MouseButton.SECONDARY)
                        return;

                    double offsetX = mouseDragEvent.getScreenX() - dragData.initialMouseX;
                    double offsetY = mouseDragEvent.getScreenY() - dragData.initialMouseY;

                    dragData.draggedPieceView.setTranslateX(dragData.initialTranslateX + offsetX);
                    dragData.draggedPieceView.setTranslateY(dragData.initialTranslateY + offsetY);
                    mouseDragEvent.consume();
                });
                tile.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleaseEvent -> {
                    if(mouseReleaseEvent.getButton() == MouseButton.SECONDARY) return;
                    int tileX = (int) (mouseReleaseEvent.getX()/tile.getWidth()), tileY = (int) (mouseReleaseEvent.getY()/tile.getHeight());
                    if(mouseReleaseEvent.getX() < 0) tileX--; if(mouseReleaseEvent.getY() < 0) tileY--;

                    int destinationColumn = tile.model.getColumn() + tileX;
                    int destinationRow = tile.model.getRow() - tileY;

                    try{ handleMove(fetchTile(position.model.fetchTile(destinationColumn, destinationRow)), mouseReleaseEvent);}
                    catch (IllegalArgumentException e) { Board.this.reload(); }

                    mouseReleaseEvent.consume();
                });
            }
        }
        private void deactivate() {
            for (Node tile : getChildren()) tile.removeEventHandler(MouseEvent.MOUSE_PRESSED, getOnMousePressed());
            for (Node tile : getChildren()) tile.removeEventHandler(MouseEvent.MOUSE_DRAGGED, getOnMouseDragged());
            for (Node tile : getChildren()) tile.removeEventHandler(MouseEvent.MOUSE_RELEASED, getOnMouseReleased());
        }

        private void refresh() {
            for(Node node : getChildren()) ((Tile)node).refresh();
        }

        private void update(data.Position model) {
            if(tempSelectedTile != null) tempSelectedTile.deselect();
            fetchTile(this.model.locateKing(this.model.turn)).bgLayer.getStyleClass().clear();

            this.model = model;
            Iterator<data.Position.Tile> tileIterator = model.getTilesOfBoard().listIterator();
            for(Node node : getChildren()) ((Tile) node).update(tileIterator.next());

            if(model.isKingChecked(model.turn))
                fetchTile(model.locateKing(model.turn)).bgLayer.getStyleClass().add("check");
        }
    }
    private class PromotionOptions extends GridPane {
        private final Pane promotionVeil;
        private PromotionOptions(Tile finalTile) {
            listenForEvents = false;
            promotionVeil = new Pane();
            promotionVeil.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                Board.this.getChildren().remove(PromotionOptions.this);
                Board.this.getChildren().remove(promotionVeil);
                listenForEvents = true;
                refresh();
            });
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
                    update(); listenForEvents = true;
                });
                add(option, 0, rowIndex++);
            }

            setGridLinesVisible(true);
            setMaxSize(Board.this.getWidth()/8, Board.this.getHeight()/2);

            getStyleClass().add("promotion-options");
        }
    }

    private final ImageView background;
    private Position position;

    private MoveHistory moveHistory;

    private final EventHandler<KeyEvent> arrowKeyPress = keyEvent -> {
        if(listenForEvents) {
            switch (keyEvent.getCode()) {
                case LEFT:
                    moveHistory.undoMove();
                    break;
                case RIGHT:
                    moveHistory.redoMove();
                    break;
                case UP:
                    moveHistory.undoAll();
                    break;
                case DOWN:
                    moveHistory.redoAll();
                    break;
                default:
            }
            jumpToPosition(moveHistory.getCurrentGame().getCurrentPosition());
        }
    };

    public Board() {
        position = new Position(new data.Position());

        background = new ImageView( new Image("boardthemes/" + Preferences.userRoot().get("Board Theme", "Green") + ".png",
                        560, 560, true, true));
        background.fitWidthProperty().bindBidirectional(position.prefWidthProperty());
        background.fitHeightProperty().bindBidirectional(position.prefHeightProperty());

        getChildren().addAll(background, position);
        getStyleClass().add("board");

        getStylesheets().add("/css/board.css");
    }
    @Override
    public void resize(double width, double height) {
        //System.out.println("Resize method was called with params(W,H): " + width + ", " + height);
        double size = Math.min(width, height);
        super.resize(size, size);
    }

    public Tile fetchTile(data.Position.Tile tileModel) {
        for(Node node : position.getChildren())
            if(((Tile)node).model == tileModel) return (Tile) node;
        throw new RuntimeException("Could not find the requested tile.");
    }

    public void jumpToPosition(data.Position positionModel) {
        position.update(positionModel);
    }

    private void handleMove(Tile clickedTile, MouseEvent mouseEvent) {
        if (!listenForEvents) return;

        data.Position.Tile clickedTileModel = clickedTile.model;
        if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && clickedTileModel.getPieceOnTile() != null &&
                new Move(clickedTileModel.getPieceOnTile(), position.model).getMoveType() != Move.MoveType.INVALID) {
            dragData = new DragData(clickedTile, mouseEvent);
            if(!clickedTile.equals(permSelectedTile)) permSelectedTile = null;
        }
        else if(moveToMake != null) {
            moveToMake.setFinalTile(clickedTileModel);
            if(moveToMake.getMoveType() == Move.MoveType.INVALID) {
                if(permSelectedTile == null && dragData != null && dragData.dragSource.equals(clickedTile))
                    permSelectedTile = clickedTile;
                else {
                    if(tempSelectedTile!=null) tempSelectedTile.deselect();
                    if(permSelectedTile!=null) permSelectedTile.deselect();
                    permSelectedTile = null;
                }
                refresh();
            }
            else {
                if (moveToMake.getMoveType() == Move.MoveType.PROMOTION)
                    getChildren().add(new PromotionOptions(clickedTile));
                else update();
            }
        }
    }

    private void update() {
        if(moveToMake == null)
            throw new RuntimeException("Nothing to update here!");

        if(dragData != null) {
            getChildren().remove(dragData.draggedPieceView);
            dragData = null;
        }

        moveHistory.addMove(moveToMake);
        position.update(moveToMake.getFinalPosition());
        moveToMake = null;

        if(position.model.getPiecesThatCanMove().isEmpty()) position.deactivate();
    }
    public void refresh() {
        if(dragData != null) {
            getChildren().remove(dragData.draggedPieceView);
            dragData = null; tempSelectedTile = null;
        }

        position.refresh();
    }
    void reload() {
        pieceset = new Image[2][6];
        background.setImage(new Image("boardthemes/" + Preferences.userRoot().get("Board Theme", "Green") + ".png",
                560, 560, true, true));
        getChildren().remove(0, getChildren().size());
        getChildren().addAll(background, position);
        if(dragData != null) {
            getChildren().remove(dragData.draggedPieceView);
            dragData = null;
        }
        position.refresh();
        if(tempSelectedTile!=null) tempSelectedTile.deselect();
        if(permSelectedTile!=null) permSelectedTile.deselect();
    }

    public MoveHistory startGame() {
        position = new Position(new data.Position());
        position.model.arrange();
        reload();

        position.activate();
        addEventHandler(KeyEvent.KEY_PRESSED, arrowKeyPress);

        return moveHistory = new MoveHistory(this, position.model);
    }
}