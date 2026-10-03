package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.PetTalentCalculator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;
import java.util.TreeMap;

public class PetPlannerController {
    @FXML private TextField parentAName;
    @FXML private TextField parentBName;
    @FXML private TextField offspringName;
    @FXML private TextArea parentAPool;
    @FXML private TextArea parentBPool;
    @FXML private TreeView<String> bloodlineTree;
    @FXML private TableView<TalentResult> talentTable;
    @FXML private TableColumn<TalentResult, String> talentColumn;
    @FXML private TableColumn<TalentResult, String> chanceColumn;
    @FXML private Label modelNote;

    @FXML
    public void initialize() {
        talentColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().talent()));
        chanceColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(String.format("%.1f%%", cell.getValue().chance())));
        parentAPool.setText("Mighty, 35\nPain-Giver, 20\nCritical Striker, 15");
        parentBPool.setText("Mighty, 25\nPain-Giver, 30\nSpell-Proof, 20");
        parentAName.setText("Parent A");
        parentBName.setText("Parent B");
        offspringName.setText("New pet");
        bloodlineTree.setRoot(new TreeItem<>("Pet bloodline"));
        modelNote.setText("Illustrative model: parent pool percentages are treated as independent chances; this is not an in-game hatch prediction.");
    }

    @FXML
    private void handleCalculate() {
        try {
            Map<String, Double> poolA = parsePool(parentAPool.getText());
            Map<String, Double> poolB = parsePool(parentBPool.getText());
            Map<String, Double> chances = PetTalentCalculator.calculate(poolA, poolB);
            talentTable.getItems().setAll(chances.entrySet().stream()
                    .map(entry -> new TalentResult(entry.getKey(), entry.getValue()))
                    .toList());
            refreshBloodline(poolA, poolB, chances);
        } catch (IllegalArgumentException exception) {
            showError("Check talent pools", exception.getMessage());
        }
    }

    private Map<String, Double> parsePool(String text) {
        Map<String, Double> pool = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int lineNumber = 0;
        for (String line : text.split("\\R")) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] fields = trimmed.split(",", -1);
            if (fields.length != 2 || fields[0].trim().isEmpty()) {
                throw new IllegalArgumentException("Line " + lineNumber + " must use Talent, Percentage format.");
            }
            String talent = fields[0].trim();
            double percentage;
            try {
                percentage = Double.parseDouble(fields[1].trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Line " + lineNumber + " has an invalid percentage.");
            }
            if (pool.containsKey(talent)) {
                throw new IllegalArgumentException("Talent names must be unique within each parent's pool: " + talent);
            }
            if (percentage < 0 || percentage > 100 || !Double.isFinite(percentage)) {
                throw new IllegalArgumentException("Line " + lineNumber + " percentage must be between 0 and 100.");
            }
            pool.put(talent, percentage);
        }
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one talent for each parent.");
        }
        return pool;
    }

    private void refreshBloodline(Map<String, Double> poolA, Map<String, Double> poolB,
                                  Map<String, Double> chances) {
        TreeItem<String> root = new TreeItem<>("Generation 1");
        TreeItem<String> firstParent = new TreeItem<>(displayName(parentAName.getText(), "Parent A"));
        TreeItem<String> secondParent = new TreeItem<>(displayName(parentBName.getText(), "Parent B"));
        poolA.forEach((talent, chance) -> firstParent.getChildren().add(
                new TreeItem<>(String.format("%s (%.1f%%)", talent, chance))));
        poolB.forEach((talent, chance) -> secondParent.getChildren().add(
                new TreeItem<>(String.format("%s (%.1f%%)", talent, chance))));
        TreeItem<String> offspring = new TreeItem<>(displayName(offspringName.getText(), "New pet")
                + " — potential manifestations");
        chances.forEach((talent, chance) -> offspring.getChildren().add(
                new TreeItem<>(String.format("%s (%.1f%% model chance)", talent, chance))));
        root.getChildren().add(firstParent);
        root.getChildren().add(secondParent);
        root.getChildren().add(offspring);
        root.setExpanded(true);
        firstParent.setExpanded(true);
        secondParent.setExpanded(true);
        offspring.setExpanded(true);
        bloodlineTree.setRoot(root);
    }

    private String displayName(String name, String fallback) {
        return name == null || name.isBlank() ? fallback : name.trim();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public record TalentResult(String talent, double chance) {
    }
}
