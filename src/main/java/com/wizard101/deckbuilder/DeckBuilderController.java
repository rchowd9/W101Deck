package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.model.Card;
import com.wizard101.deckbuilder.model.Deck;
import com.wizard101.deckbuilder.service.CardDatabase;
import com.wizard101.deckbuilder.service.PlannerRepository;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;

public class DeckBuilderController {

    @FXML private Label cardCountLabel;
    @FXML private Label avgPipLabel;
    @FXML private ComboBox<String> schoolFilter;
    @FXML private ComboBox<String> savedDeckSelector;
    @FXML private TextField deckNameField;
    @FXML private TilePane deckTilePane;
    @FXML private TilePane catalogTilePane;

    private static final int MAX_DECK_SIZE = 64;
    private final Deck deck = new Deck(MAX_DECK_SIZE);
    private final CardDatabase cardDatabase = new CardDatabase();
    private final PlannerRepository repository = PlannerRepository.getInstance();

    @FXML
    public void initialize() {
        schoolFilter.getItems().addAll(
                "All Schools", "Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance");
        schoolFilter.getSelectionModel().selectFirst();
        refreshCatalog();
        updateDeckStats();
        refreshSavedDecks();
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

    @FXML
    private void handleSaveDeck() {
        String name = deckNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Name your deck", "Enter a name before saving this deck build.");
            return;
        }
        try {
            repository.saveDeck(name, deck.getCards());
            refreshSavedDecks();
            savedDeckSelector.getSelectionModel().select(name);
        } catch (SQLException exception) {
            showError("Could not save deck", exception.getMessage());
        }
    }

    @FXML
    private void handleLoadDeck() {
        String name = savedDeckSelector.getValue();
        if (name == null) {
            showError("Choose a saved deck", "Select a saved build before loading it.");
            return;
        }
        try {
            var savedCards = repository.loadDeck(name);
            if (savedCards.size() > MAX_DECK_SIZE) {
                showError("Saved deck exceeds capacity", "This build contains more than "
                        + MAX_DECK_SIZE + " cards and cannot be loaded.");
                return;
            }
            deck.clear();
            for (Card card : savedCards) {
                if (!deck.addCard(card)) {
                    throw new IllegalStateException("A saved deck could not fit into the current deck.");
                }
            }
            refreshDeck();
            refreshCatalog();
            updateDeckStats();
        } catch (SQLException exception) {
            showError("Could not load deck", exception.getMessage());
        }
    }

    @FXML
    private void handleExportDeck() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export deck build");
        chooser.setInitialFileName("wizard101-deck.txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
        Window window = deckTilePane.getScene().getWindow();
        var destination = chooser.showSaveDialog(window);
        if (destination == null) {
            return;
        }
        StringBuilder export = new StringBuilder("Wizard101 deck build\n");
        export.append("Cards: ").append(deck.getSize()).append(" / ").append(MAX_DECK_SIZE).append('\n');
        export.append(String.format("Average pip cost: %.1f%n%n", deck.getAveragePipCost()));
        for (Card card : deck.getCards()) {
            export.append(card.getName()).append(" | ").append(card.getSchool())
                    .append(" | ").append(card.getPipCost()).append(" pips")
                    .append(" | ").append(card.getDescription()).append('\n');
        }
        try {
            Files.writeString(destination.toPath(), export.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            showError("Could not export deck", exception.getMessage());
        }
    }

    private void refreshSavedDecks() {
        try {
            String selected = savedDeckSelector.getValue();
            savedDeckSelector.setItems(FXCollections.observableArrayList(repository.getSavedDeckNames()));
            if (selected != null && savedDeckSelector.getItems().contains(selected)) {
                savedDeckSelector.getSelectionModel().select(selected);
            }
        } catch (SQLException exception) {
            showError("Could not load saved decks", exception.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "An unexpected error occurred." : message);
        alert.showAndWait();
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