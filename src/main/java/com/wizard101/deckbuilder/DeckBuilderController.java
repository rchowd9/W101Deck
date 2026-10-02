package com.wizard101.deckbuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import java.util.ArrayList;
import java.util.List;

public class DeckBuilderController {

    @FXML private Label cardCountLabel;
    @FXML private Label avgPipLabel;
    @FXML private ComboBox<String> schoolFilter;
    @FXML private TilePane deckTilePane;

    private final List<String> currentDeck = new ArrayList<>();
    private final int MAX_DECK_SIZE = 64;

    @FXML
    public void initialize() {
        schoolFilter.getItems().addAll("Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance");
        updateDeckStats();
    }

    @FXML
    private void handleResetDeck() {
        currentDeck.clear();
        deckTilePane.getChildren().clear();
        updateDeckStats();
    }

    private void updateDeckStats() {
        cardCountLabel.setText("Total Cards: " + currentDeck.size() + " / " + MAX_DECK_SIZE);
        // Additional pip economy calculations can be bound here
    }
}