package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.model.Card;
import com.wizard101.deckbuilder.service.CombatCalculator;
import com.wizard101.deckbuilder.service.CommunityBuildCatalog;
import com.wizard101.deckbuilder.service.CommunityBuildCatalog.Build;
import com.wizard101.deckbuilder.service.PlannerRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ToolIntegrationController {
    private static final String CURRENT_DECK = "Current deck";

    @FXML private ComboBox<String> buildSchoolFilter;
    @FXML private ComboBox<String> buildLevelFilter;
    @FXML private ComboBox<String> buildRoleFilter;
    @FXML private ListView<Build> communityBuildList;
    @FXML private Label buildDescription;
    @FXML private ComboBox<String> compareDeckSelector;
    @FXML private Label comparisonResult;
    @FXML private Label comparisonDetails;
    @FXML private TextField baseDamageField;
    @FXML private TextField wizardDamageField;
    @FXML private TextField pierceField;
    @FXML private TextField targetResistField;
    @FXML private TextField bladeField;
    @FXML private TextField trapField;
    @FXML private TextField targetHealthField;
    @FXML private Label comboResult;

    private final PlannerRepository repository = PlannerRepository.getInstance();
    private List<Card> currentDeck;

    @FXML
    public void initialize() {
        buildSchoolFilter.getItems().addAll("All Schools", "Storm", "Life", "Death", "Balance");
        buildSchoolFilter.getSelectionModel().selectFirst();
        buildLevelFilter.getItems().addAll("All levels", "1-49", "50-99", "100-149", "150+");
        buildLevelFilter.getSelectionModel().selectFirst();
        buildRoleFilter.getItems().addAll("All roles", "PvE AoE", "Boss hitter", "Team support", "Solo utility");
        buildRoleFilter.getSelectionModel().selectFirst();
        buildSchoolFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshBuilds());
        buildLevelFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshBuilds());
        buildRoleFilter.valueProperty().addListener((observable, oldValue, newValue) -> refreshBuilds());
        communityBuildList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBuild(newValue));
        compareDeckSelector.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateComparison());
        refreshBuilds();
        refreshDeckChoices();
    }

    public void setCurrentDeck(List<Card> cards) {
        currentDeck = cards;
        refreshDeckChoices();
    }

    public void refreshDeckChoices() {
        String selected = compareDeckSelector.getValue();
        List<String> choices = new ArrayList<>();
        if (currentDeck != null) {
            choices.add(CURRENT_DECK);
        }
        try {
            choices.addAll(repository.getSavedDeckNames());
        } catch (SQLException exception) {
            showError("Could not load saved decks", exception.getMessage());
        }
        compareDeckSelector.setItems(FXCollections.observableArrayList(choices));
        if (selected != null && choices.contains(selected)) {
            compareDeckSelector.getSelectionModel().select(selected);
        } else if (!choices.isEmpty()) {
            compareDeckSelector.getSelectionModel().selectFirst();
        } else {
            comparisonResult.setText("Save or build a deck to compare it with a sample.");
            comparisonDetails.setText("");
        }
        updateComparison();
    }

    @FXML
    private void handleCompareBuild() {
        updateComparison();
    }

    @FXML
    private void handleCalculateCombos() {
        try {
            CombatCalculator.Result result = CombatCalculator.calculate(new CombatCalculator.Inputs(
                    number(wizardDamageField), number(pierceField), 0, 0,
                    number(targetHealthField), number(targetResistField),
                    number(baseDamageField), 100, number(bladeField), number(trapField), null));
            StringBuilder output = new StringBuilder();
            for (CombatCalculator.Combo combo : result.combos()) {
                if (!output.isEmpty()) {
                    output.append('\n');
                }
                output.append(combo.name()).append(": ")
                        .append(String.format(Locale.ROOT, "%.0f", combo.estimatedDamage()))
                        .append(" damage");
            }
            CombatCalculator.Combo recommended = result.recommendedCombo();
            comboResult.setText((recommended == null
                    ? "No configured combo reaches the target health.\n"
                    : "Most efficient: " + recommended.name() + " (" + String.format(Locale.ROOT, "%.0f",
                            recommended.estimatedDamage()) + " damage)\n") + output);
        } catch (IllegalArgumentException exception) {
            comboResult.setText(exception.getMessage());
        }
    }

    @FXML
    private void handleExportDeck() {
        try {
            List<Card> cards = getSelectedDeck();
            if (cards == null) {
                showError("Choose a deck", "Select your current deck or a saved deck before exporting.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export deck for comparison");
            chooser.setInitialFileName("wizard101-deck.txt");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
            Window window = communityBuildList.getScene().getWindow();
            var destination = chooser.showSaveDialog(window);
            if (destination == null) {
                return;
            }
            StringBuilder export = new StringBuilder("Wizard101 deck build\n");
            export.append("Cards: ").append(cards.size()).append('\n');
            for (Card card : cards) {
                export.append(card.getName()).append(" | ").append(card.getSchool())
                        .append(" | ").append(card.getPipCost()).append(" pips")
                        .append(" | ").append(card.getRole().getDisplayName()).append('\n');
            }
            Files.writeString(destination.toPath(), export.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            showError("Could not export deck", exception.getMessage());
        } catch (SQLException exception) {
            showError("Could not load deck", exception.getMessage());
        }
    }

    private void refreshBuilds() {
        Build selected = communityBuildList.getSelectionModel().getSelectedItem();
        List<Build> matches = CommunityBuildCatalog.getBuilds().stream()
                .filter(build -> "All Schools".equals(buildSchoolFilter.getValue())
                        || build.school().equals(buildSchoolFilter.getValue()))
                .filter(build -> matchesLevel(build.level(), buildLevelFilter.getValue()))
                .filter(build -> "All roles".equals(buildRoleFilter.getValue())
                        || build.role().equals(buildRoleFilter.getValue()))
                .toList();
        communityBuildList.setItems(FXCollections.observableArrayList(matches));
        if (selected != null && matches.contains(selected)) {
            communityBuildList.getSelectionModel().select(selected);
        } else if (!matches.isEmpty()) {
            communityBuildList.getSelectionModel().selectFirst();
        } else {
            showBuild(null);
        }
    }

    private boolean matchesLevel(int level, String range) {
        if (range == null || "All levels".equals(range)) {
            return true;
        }
        return switch (range) {
            case "1-49" -> level <= 49;
            case "50-99" -> level >= 50 && level <= 99;
            case "100-149" -> level >= 100 && level <= 149;
            case "150+" -> level >= 150;
            default -> false;
        };
    }

    private void showBuild(Build build) {
        if (build == null) {
            buildDescription.setText("No sample builds match these filters.");
        } else {
            buildDescription.setText(build.school() + " · Level " + build.level() + " · " + build.role()
                    + "\n" + build.description() + "\nCards: " + String.join(", ", build.cardNames()));
        }
        updateComparison();
    }

    private void updateComparison() {
        Build build = communityBuildList.getSelectionModel().getSelectedItem();
        if (build == null) {
            comparisonResult.setText("Choose a sample build to compare.");
            comparisonDetails.setText("");
            return;
        }
        try {
            List<Card> cards = getSelectedDeck();
            if (cards == null) {
                comparisonResult.setText("Choose or save a deck to compare.");
                comparisonDetails.setText("");
                return;
            }
            Map<String, Integer> sampleCounts = counts(build.cardNames());
            Map<String, Integer> deckCounts = new LinkedHashMap<>();
            for (Card card : cards) {
                deckCounts.merge(card.getName(), 1, Integer::sum);
            }
            int shared = sampleCounts.entrySet().stream()
                    .mapToInt(entry -> Math.min(entry.getValue(), deckCounts.getOrDefault(entry.getKey(), 0)))
                    .sum();
            int sampleOnly = sampleCounts.values().stream().mapToInt(Integer::intValue).sum() - shared;
            int deckOnly = cards.size() - shared;
            comparisonResult.setText(shared + " shared card(s) · " + sampleOnly
                    + " sample-only · " + deckOnly + " deck-only");
            comparisonDetails.setText("Sample contains " + sampleCounts.values().stream()
                    .mapToInt(Integer::intValue).sum() + " cards; selected deck contains " + cards.size() + ".");
        } catch (SQLException exception) {
            comparisonResult.setText("Could not compare this deck.");
            comparisonDetails.setText(exception.getMessage());
            showError("Could not compare deck", exception.getMessage());
        }
    }

    private List<Card> getSelectedDeck() throws SQLException {
        String selection = compareDeckSelector.getValue();
        if (selection == null) {
            return null;
        }
        if (CURRENT_DECK.equals(selection)) {
            return currentDeck;
        }
        return repository.loadDeck(selection);
    }

    private static Map<String, Integer> counts(List<String> names) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        names.forEach(name -> counts.merge(name, 1, Integer::sum));
        return counts;
    }

    private static double number(TextField field) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number for " + field.getPromptText() + ".");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "An unexpected error occurred." : message);
        alert.showAndWait();
    }
}
