package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.model.Card;
import com.wizard101.deckbuilder.model.Deck;
import com.wizard101.deckbuilder.service.CardDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class DeckBuilderController {

    @FXML private Label cardCountLabel;
    @FXML private Label avgPipLabel;
    @FXML private ComboBox<String> schoolFilter;
    @FXML private TilePane deckTilePane;
    @FXML private TilePane catalogTilePane;

    private static final int MAX_DECK_SIZE = 64;
    private final Deck deck = new Deck(MAX_DECK_SIZE);
    private final CardDatabase cardDatabase = new CardDatabase();

    @FXML
    public void initialize() {
        schoolFilter.getItems().addAll(
                "All Schools", "Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance");
        schoolFilter.getSelectionModel().selectFirst();
        refreshCatalog();
        updateDeckStats();
    }

    @FXML
    private void handleResetDeck() {
        deck.clear();
        refreshDeck();
        refreshCatalog();
        updateDeckStats();
    }

    @FXML
    private void handleSchoolFilter() {
        refreshCatalog();
    }

    private void refreshCatalog() {
        catalogTilePane.getChildren().clear();
        String selectedSchool = schoolFilter.getValue();

        for (Card card : cardDatabase.getAllSpells()) {
            if (!"All Schools".equals(selectedSchool) && !card.getSchool().equals(selectedSchool)) {
                continue;
            }
            catalogTilePane.getChildren().add(createSpellCard(card));
        }
    }

    private VBox createSpellCard(Card card) {
        VBox spellCard = new VBox(8);
        spellCard.getStyleClass().add("spell-card");
        Label name = new Label(card.getName());
        name.getStyleClass().add("spell-name");
        Label details = new Label(card.getSchool() + "  |  " + card.getPipCost() + " pips");
        Label description = new Label(card.getDescription());
        description.setWrapText(true);
        Button addButton = new Button("Add to deck");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setDisable(deck.getSize() >= MAX_DECK_SIZE);
        addButton.setOnAction(event -> {
            if (deck.addCard(card)) {
                refreshDeck();
                updateDeckStats();
                refreshCatalog();
            }
        });
        spellCard.getChildren().addAll(name, details, description, addButton);
        return spellCard;
    }

    private void refreshDeck() {
        deckTilePane.getChildren().clear();
        for (int index = 0; index < deck.getCards().size(); index++) {
            Card card = deck.getCards().get(index);
            VBox deckCard = new VBox(6);
            deckCard.getStyleClass().add("deck-card");
            Label name = new Label(card.getName());
            name.getStyleClass().add("spell-name");
            Label details = new Label(card.getSchool() + "  |  " + card.getPipCost() + " pips");
            Button removeButton = new Button("Remove");
            removeButton.setOnAction(event -> {
                deck.removeCard(card);
                refreshDeck();
                updateDeckStats();
                refreshCatalog();
            });
            deckCard.getChildren().addAll(name, details, removeButton);
            deckTilePane.getChildren().add(deckCard);
        }
    }

    private void updateDeckStats() {
        cardCountLabel.setText("Total Cards: " + deck.getSize() + " / " + MAX_DECK_SIZE);
        avgPipLabel.setText(String.format("Avg Pip Cost: %.1f", deck.getAveragePipCost()));
    }
}