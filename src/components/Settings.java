package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import app.Display;

import java.io.File;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class Settings extends Stage {
    Display mainDisplay;
    ChoiceBox<String> availableBoardThemes, availablePieceThemes;
    public Settings(Display mainDisplay) {
        this.mainDisplay = mainDisplay;

        setTitle("Settings");
        initOwner(mainDisplay.getScene().getWindow());
        initModality(Modality.APPLICATION_MODAL);

        Preferences preferences = Preferences.userRoot();
        VBox appearanceOptions = new VBox(
                getPieceThemeSelector(preferences.get("Piece Theme", "Neo")),
                getBoardThemeSelector(preferences.get("Board Theme", "Green")),
                getOkCancelApplyButtons()
        );
        appearanceOptions.setAlignment(Pos.CENTER);
        appearanceOptions.setSpacing(10);

        setScene(new Scene(appearanceOptions));
    }

    private HBox getPieceThemeSelector(String savedTheme) {
        HBox pieceThemeSelector = new HBox();
        VBox.setMargin(pieceThemeSelector, new Insets(30));
        pieceThemeSelector.setAlignment(Pos.CENTER);
        pieceThemeSelector.setSpacing(10);

        Label label = new Label("Piece Theme: ");
        availablePieceThemes = new ChoiceBox<>();
        availablePieceThemes.getItems().addAll(Arrays.asList(new File("res/piecethemes").listFiles())
                .stream().map(file -> file.getName()).collect(Collectors.toList()));
        availablePieceThemes.setValue(savedTheme);

        pieceThemeSelector.getChildren().addAll(label, availablePieceThemes);
        return pieceThemeSelector;
    }
    private HBox getBoardThemeSelector(String savedTheme) {
        HBox boardThemeSelector = new HBox();
        VBox.setMargin(boardThemeSelector, new Insets(30));
        boardThemeSelector.setSpacing(10);
        boardThemeSelector.setAlignment(Pos.CENTER);

        Label label = new Label("Board Theme: ");
        availableBoardThemes = new ChoiceBox<>();
        availableBoardThemes.getItems().addAll(Arrays.asList(new File("res/boardthemes").listFiles())
                .stream().map(file -> file.getName().split("\\.")[0]).collect(Collectors.toList()));
        availableBoardThemes.setValue(savedTheme);

        boardThemeSelector.getChildren().addAll(label, availableBoardThemes);
        return boardThemeSelector;
    }

    private HBox getOkCancelApplyButtons() {
        HBox okCancelApplyButtons = new HBox();
        VBox.setMargin(okCancelApplyButtons, new Insets(30));
        okCancelApplyButtons.setSpacing(10);
        okCancelApplyButtons.setAlignment(Pos.CENTER);

        Button applyButton = new Button("Apply");
        Button cancelButton = new Button("Cancel");
        Button okButton = new Button("OK");

        applyButton.setOnAction(actionEvent -> {
            Preferences preferences = Preferences.userRoot();
            preferences.put("Piece Theme", availablePieceThemes.getValue());
            preferences.put("Board Theme", availableBoardThemes.getValue());
            mainDisplay.getBoard().refresh();
        });
        cancelButton.setOnAction(actionEvent -> close());
        okButton.setOnAction(actionEvent -> {
            applyButton.fire();
            cancelButton.fire();
        });

        okCancelApplyButtons.getChildren().addAll(okButton, cancelButton, applyButton);
        return okCancelApplyButtons;
    }
}
