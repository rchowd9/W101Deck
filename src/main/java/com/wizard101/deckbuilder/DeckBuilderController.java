package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.model.Card;
import com.wizard101.deckbuilder.model.Deck;
import com.wizard101.deckbuilder.service.CardDatabase;
import com.wizard101.deckbuilder.service.PlannerRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DeckBuilderController {

    @FXML private Label cardCountLabel;
    @FXML private Label avgPipLabel;
    @FXML private Label analysisSummaryLabel;
    @FXML private TextField catalogSearchField;
    @FXML private ComboBox<String> schoolFilter;
    @FXML private ComboBox<String> savedDeckSelector;
    @FXML private TextField deckNameField;
    @FXML private TextField encounterField;
    @FXML private TextArea notesArea;
    @FXML private TilePane deckTilePane;
    @FXML private TilePane catalogTilePane;
    @FXML private TilePane handTilePane;
    @FXML private Button mulliganButton;
    @FXML private VBox analysisBox;
    @FXML private BarChart<String, Number> pipCurveChart;
    @FXML private CategoryAxis pipCostAxis;
    @FXML private NumberAxis pipCountAxis;

    private static final int MAX_DECK_SIZE = 64;
    private final Deck deck = new Deck(MAX_DECK_SIZE);
    private final CardDatabase cardDatabase = new CardDatabase();
    private final PlannerRepository repository = PlannerRepository.getInstance();
    private final List<Card> openingHand = new ArrayList<>();
    private final Set<Integer> mulliganIndexes = new HashSet<>();
    private final Random random = new Random();

    @FXML
    public void initialize() {
        schoolFilter.getItems().addAll(
                "All Schools", "Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance");
        schoolFilter.getSelectionModel().selectFirst();
        catalogSearchField.textProperty().addListener(
                (observable, oldValue, newValue) -> refreshCatalog());
        refreshCatalog();
        updateDeckStats();
        updateDeckAnalysis();
        refreshSavedDecks();
    }

    @FXML
    private void handleResetDeck() {
        deck.clear();
        openingHand.clear();
        mulliganIndexes.clear();
        handTilePane.getChildren().clear();
        mulliganButton.setVisible(false);
        mulliganButton.setManaged(false);
        refreshDeck();
        refreshCatalog();
        updateDeckStats();
        updateDeckAnalysis();
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
            openingHand.clear();
            mulliganIndexes.clear();
            handTilePane.getChildren().clear();
            mulliganButton.setVisible(false);
            mulliganButton.setManaged(false);
            refreshDeck();
            refreshCatalog();
            updateDeckStats();
            updateDeckAnalysis();
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
        export.append("Encounter: ").append(encounterField.getText()).append('\n');
        export.append("Notes: ").append(notesArea.getText()).append("\n\n");
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

    @FXML
    private void handleDrawOpeningHand() {
        if (deck.getCards().isEmpty()) {
            showError("No cards in deck", "Add a few spells before drawing an opening hand.");
            return;
        }

        openingHand.clear();
        mulliganIndexes.clear();

        List<Card> shuffledCards = new ArrayList<>(deck.getCards());
        Collections.shuffle(shuffledCards, random);

        int handSize = Math.min(7, shuffledCards.size());
        openingHand.addAll(shuffledCards.subList(0, handSize));
        renderOpeningHand();
    }

    @FXML
    private void handleMulliganSelected() {
        if (openingHand.isEmpty() || mulliganIndexes.isEmpty()) {
            return;
        }

        List<Card> redrawPool = new ArrayList<>(deck.getCards());
        List<Integer> selectedIndexes = new ArrayList<>(mulliganIndexes);
        Collections.sort(selectedIndexes);

        for (int index : selectedIndexes) {
            if (redrawPool.isEmpty()) {
                break;
            }
            int replacementIndex = random.nextInt(redrawPool.size());
            Card replacement = redrawPool.remove(replacementIndex);
            if (index >= 0 && index < openingHand.size()) {
                openingHand.set(index, replacement);
            }
        }

        mulliganIndexes.clear();
        renderOpeningHand();
    }

    private void updateMulliganButtonState() {
        boolean hasHand = !openingHand.isEmpty();
        boolean hasSelection = !mulliganIndexes.isEmpty();
        mulliganButton.setVisible(hasHand);
        mulliganButton.setManaged(hasHand);
        mulliganButton.setDisable(!hasSelection);
        mulliganButton.setText(hasSelection ? "Redraw selected cards" : "Select cards to mulligan");
    }

    private void renderOpeningHand() {
        handTilePane.getChildren().clear();
        updateMulliganButtonState();

        for (int index = 0; index < openingHand.size(); index++) {
            Card card = openingHand.get(index);
            int cardIndex = index;
            VBox handCard = new VBox(6);
            handCard.getStyleClass().add("hand-card");
            Label name = new Label(card.getName());
            name.getStyleClass().add("spell-name");
            Label details = new Label(card.getSchool() + "  |  " + card.getPipCost() + " pips");
            CheckBox mulliganCheck = new CheckBox("Mulligan");
            mulliganCheck.setSelected(mulliganIndexes.contains(cardIndex));
            mulliganCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    mulliganIndexes.add(cardIndex);
                } else {
                    mulliganIndexes.remove(cardIndex);
                }
                updateMulliganButtonState();
            });
            handCard.getChildren().addAll(name, details, mulliganCheck);
            handTilePane.getChildren().add(handCard);
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
        String searchTerm = catalogSearchField.getText().trim().toLowerCase(Locale.ROOT);

        for (Card card : cardDatabase.getAllSpells()) {
            if (!"All Schools".equals(selectedSchool) && !card.getSchool().equals(selectedSchool)) {
                continue;
            }
            if (!searchTerm.isEmpty()
                    && !card.getName().toLowerCase(Locale.ROOT).contains(searchTerm)
                    && !card.getSchool().toLowerCase(Locale.ROOT).contains(searchTerm)
                    && !card.getDescription().toLowerCase(Locale.ROOT).contains(searchTerm)) {
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
                updateDeckAnalysis();
                refreshCatalog();
            }
        });
        spellCard.getChildren().addAll(name, details, description, addButton);
        return spellCard;
    }

    private void refreshDeck() {
        deckTilePane.getChildren().clear();
        for (Card card : deck.getCards()) {
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
                updateDeckAnalysis();
                refreshCatalog();
            });
            deckCard.getChildren().addAll(name, details, removeButton);
            deckTilePane.getChildren().add(deckCard);
        }
    }

    private void updateDeckStats() {
        cardCountLabel.setText("Total Cards: " + deck.getSize() + " / " + MAX_DECK_SIZE);
        avgPipLabel.setText(String.format("Avg Pip Cost: %.1f", deck.getAveragePipCost()));
        updatePipCurve();
    }

    private void updatePipCurve() {
        Map<Integer, Integer> pipCounts = new LinkedHashMap<>();
        int highestPipCost = 4;
        for (Card card : deck.getCards()) {
            pipCounts.merge(card.getPipCost(), 1, Integer::sum);
            highestPipCost = Math.max(highestPipCost, card.getPipCost());
        }

        List<String> pipCosts = new ArrayList<>();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int highestCount = 0;
        for (int pipCost = 0; pipCost <= highestPipCost; pipCost++) {
            int count = pipCounts.getOrDefault(pipCost, 0);
            pipCosts.add(String.valueOf(pipCost));
            series.getData().add(new XYChart.Data<>(String.valueOf(pipCost), count));
            highestCount = Math.max(highestCount, count);
        }

        pipCurveChart.getData().setAll(series);
        pipCostAxis.setCategories(FXCollections.observableArrayList(pipCosts));
        pipCountAxis.setAutoRanging(false);
        pipCountAxis.setLowerBound(0);
        pipCountAxis.setUpperBound(Math.max(1, highestCount));
        pipCountAxis.setTickUnit(Math.max(1, Math.ceil(Math.max(1, highestCount) / 4.0)));
        pipCurveChart.setTitle(deck.getCards().isEmpty()
                ? "Add cards to see your pip curve" : "Cards by pip cost");
        pipCurveChart.setAccessibleText("Deck pip curve: " + pipCounts);
    }

    private void updateDeckAnalysis() {
        analysisBox.getChildren().clear();
        if (deck.getCards().isEmpty()) {
            analysisSummaryLabel.setText("0 unique · 0 duplicates");
            Label empty = new Label("Add cards to see this breakdown.");
            empty.getStyleClass().add("muted-text");
            analysisBox.getChildren().add(empty);
            return;
        }

        int uniqueCards = (int) deck.getCards().stream().map(Card::getName).distinct().count();
        int duplicateCards = deck.getCards().size() - uniqueCards;
        analysisSummaryLabel.setText(uniqueCards + " unique · " + duplicateCards + " duplicate" + (duplicateCards == 1 ? "" : "s"));

        Map<String, Integer> schoolCounts = new LinkedHashMap<>();
        for (Card card : deck.getCards()) {
            schoolCounts.merge(card.getSchool(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : schoolCounts.entrySet()) {
            HBox row = new HBox(8);
            Label school = new Label(entry.getKey());
            Label count = new Label(String.valueOf(entry.getValue()));
            school.setMinWidth(120);
            count.setMinWidth(32);
            row.getChildren().addAll(school, count);
            analysisBox.getChildren().add(row);
        }
    }
}