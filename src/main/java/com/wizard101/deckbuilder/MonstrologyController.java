package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.PlannerRepository;
import com.wizard101.deckbuilder.service.PlannerRepository.MonstrologyEntry;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MonstrologyController {
    @FXML private ComboBox<String> characterSelector;
    @FXML private TextField entryFilter;
    @FXML private Label progressSummary;
    @FXML private ProgressBar extractProgress;
    @FXML private TableView<MonstrologyEntry> entryTable;
    @FXML private TableColumn<MonstrologyEntry, String> creatureColumn;
    @FXML private TableColumn<MonstrologyEntry, String> animusColumn;
    @FXML private TableColumn<MonstrologyEntry, String> locationColumn;
    @FXML private TableColumn<MonstrologyEntry, String> targetColumn;
    @FXML private TableColumn<MonstrologyEntry, String> extractedColumn;
    @FXML private TableColumn<MonstrologyEntry, String> remainingColumn;

    private final PlannerRepository repository = PlannerRepository.getInstance();
    private FilteredList<MonstrologyEntry> filteredEntries;

    @FXML
    public void initialize() {
        creatureColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().creature()));
        animusColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().animusType()));
        locationColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().location()));
        targetColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(cell.getValue().targetCount())));
        extractedColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(cell.getValue().extractedCount())));
        remainingColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(Math.max(
                        0, cell.getValue().targetCount() - cell.getValue().extractedCount()))));
        extractedColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        extractedColumn.setOnEditCommit(event ->
                updateExtractedCount(event.getRowValue(), event.getNewValue()));

        filteredEntries = new FilteredList<>(
                FXCollections.observableArrayList(),
                entry -> true);
        entryTable.setItems(filteredEntries);
        entryFilter.textProperty().addListener((observable, oldValue, newValue) ->
                applyFilter(newValue));
        characterSelector.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshEntries());
        refreshCharacters();
    }

    @FXML
    private void handleAddCharacter() {
        TextField name = new TextField();
        name.setPromptText("Wizard name");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add character");
        dialog.setHeaderText("Track Monstrology separately for an alt");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Character"), name);
        dialog.getDialogPane().setContent(form);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        if (name.getText().isBlank()) {
            showError("Missing character name", "Enter a name for this wizard.");
            return;
        }
        try {
            repository.addCharacter(name.getText().trim());
            refreshCharacters();
            characterSelector.getSelectionModel().select(name.getText().trim());
        } catch (SQLException exception) {
            showError("Could not save character", exception.getMessage());
        }
    }

    @FXML
    private void handleAddEntry() {
        String character = characterSelector.getValue();
        if (character == null) {
            showError("Add a character first", "Create or select a character before adding extraction goals.");
            return;
        }
        TextField creature = new TextField();
        creature.setPromptText("Creature name");
        ComboBox<String> animus = new ComboBox<>(FXCollections.observableArrayList(
                "Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance", "Star", "Moon", "Sun", "Shadow"));
        animus.setEditable(true);
        animus.setPromptText("Animus type");
        TextField location = new TextField();
        location.setPromptText("World or dungeon");
        TextField target = new TextField("1");
        TextField extracted = new TextField("0");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add extraction goal");
        dialog.setHeaderText(character + " — creature animus");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Creature"), creature);
        form.addRow(1, new Label("Animus"), animus);
        form.addRow(2, new Label("Location"), location);
        form.addRow(3, new Label("Target"), target);
        form.addRow(4, new Label("Extracted"), extracted);
        dialog.getDialogPane().setContent(form);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            if (creature.getText().isBlank() || animus.getEditor().getText().isBlank()
                    || location.getText().isBlank()) {
                throw new IllegalArgumentException("Enter a creature, animus type, and location.");
            }
            int targetCount = parseCount(target.getText(), "Target");
            int extractedCount = parseCount(extracted.getText(), "Extracted");
            repository.addMonstrologyEntry(character, creature.getText().trim(),
                    animus.getEditor().getText().trim(), extractedCount, targetCount,
                    location.getText().trim());
            refreshEntries();
        } catch (IllegalArgumentException exception) {
            showError("Invalid extraction goal", exception.getMessage());
        } catch (SQLException exception) {
            showError("Could not add extraction goal", exception.getMessage());
        }
    }

    private void updateExtractedCount(MonstrologyEntry entry, String value) {
        try {
            repository.updateMonstrologyExtractedCount(entry.id(), parseCount(value, "Extracted"));
            refreshEntries();
        } catch (IllegalArgumentException exception) {
            showError("Invalid extracted count", exception.getMessage());
            refreshEntries();
        } catch (SQLException exception) {
            showError("Could not save extraction count", exception.getMessage());
            refreshEntries();
        }
    }

    private int parseCount(String value, String label) {
        try {
            int count = Integer.parseInt(value.trim());
            if (count < 0) {
                throw new IllegalArgumentException(label + " count cannot be negative.");
            }
            return count;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " count must be a whole number.");
        }
    }

    private void refreshCharacters() {
        try {
            String selected = characterSelector.getValue();
            List<String> characters = repository.getCharacters();
            characterSelector.setItems(FXCollections.observableArrayList(characters));
            if (selected != null && characters.contains(selected)) {
                characterSelector.getSelectionModel().select(selected);
            } else if (!characters.isEmpty()) {
                characterSelector.getSelectionModel().selectFirst();
            }
        } catch (SQLException exception) {
            showError("Could not load characters", exception.getMessage());
        }
    }

    private void applyFilter(String searchText) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase();
        filteredEntries.setPredicate(entry ->
                query.isEmpty()
                        || entry.creature().toLowerCase().contains(query)
                        || entry.animusType().toLowerCase().contains(query)
                        || entry.location().toLowerCase().contains(query));
    }

    private void refreshEntries() {
        String character = characterSelector.getValue();
        if (character == null) {
            filteredEntries.getSource().clear();
            progressSummary.setText("Add a character to start tracking Monstrology.");
            extractProgress.setProgress(0);
            return;
        }
        try {
            List<MonstrologyEntry> entries = repository.getMonstrologyEntries(character);
            filteredEntries.getSource().setAll(entries);
            applyFilter(entryFilter.getText());
            int target = entries.stream().mapToInt(MonstrologyEntry::targetCount).sum();
            int extracted = entries.stream()
                    .mapToInt(entry -> Math.min(entry.targetCount(), entry.extractedCount()))
                    .sum();
            long complete = entries.stream()
                    .filter(entry -> entry.extractedCount() >= entry.targetCount())
                    .count();
            progressSummary.setText(character + " · " + entries.size() + " extraction goals · "
                    + complete + " complete · " + extracted + " / " + target + " animus collected");
            extractProgress.setProgress(target == 0 ? (entries.isEmpty() ? 0 : 1) : (double) extracted / target);
        } catch (SQLException exception) {
            showError("Could not load extraction goals", exception.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "An unexpected database error occurred." : message);
        alert.showAndWait();
    }
}
