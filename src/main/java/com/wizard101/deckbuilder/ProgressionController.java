package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.PlannerRepository;
import com.wizard101.deckbuilder.service.PlannerRepository.ProgressionItem;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProgressionController {
    private static final List<String> WORLDS = List.of(
            "Wizard City", "Krokotopia", "Marleybone", "Mooshu", "Dragonspyre",
            "Celestia", "Zafaria", "Avalon", "Azteca", "Khrysalis", "Polaris",
            "Mirage", "Empyrea", "Karamelle", "Lemuria", "Novus", "Wallaru");

    @FXML private ComboBox<String> characterSelector;
    @FXML private ComboBox<String> worldSelector;
    @FXML private ComboBox<String> categorySelector;
    @FXML private TableView<ProgressionItem> objectiveTable;
    @FXML private TableColumn<ProgressionItem, Boolean> completeColumn;
    @FXML private TableColumn<ProgressionItem, String> categoryColumn;
    @FXML private TableColumn<ProgressionItem, String> objectiveColumn;
    @FXML private Label progressSummary;
    @FXML private ProgressBar worldProgress;

    private final PlannerRepository repository = PlannerRepository.getInstance();

    @FXML
    public void initialize() {
        worldSelector.setItems(FXCollections.observableArrayList(WORLDS));
        worldSelector.getSelectionModel().selectFirst();
        categorySelector.getItems().setAll("Main quest", "Side quest", "Badge");
        categorySelector.getSelectionModel().selectFirst();
        completeColumn.setCellValueFactory(cell ->
                new ReadOnlyBooleanWrapper(cell.getValue().complete()));
        completeColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completeColumn));
        completeColumn.setOnEditCommit(event ->
                updateCompletion(event.getRowValue(), Boolean.TRUE.equals(event.getNewValue())));
        categoryColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().category()));
        objectiveColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().title()));
        characterSelector.valueProperty().addListener((observable, oldValue, newValue) -> refreshObjectives());
        worldSelector.valueProperty().addListener((observable, oldValue, newValue) -> refreshObjectives());
        try {
            characterSelector.setItems(FXCollections.observableArrayList(repository.getCharacters()));
            if (!characterSelector.getItems().isEmpty()) {
                characterSelector.getSelectionModel().selectFirst();
            }
        } catch (SQLException exception) {
            showError("Could not load characters", exception.getMessage());
        }
        refreshObjectives();
    }

    @FXML
    private void handleAddCharacter() {
        TextField name = new TextField();
        name.setPromptText("Wizard name");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add character");
        dialog.setHeaderText("Track progression separately for an alt");
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
            String character = name.getText().trim();
            repository.addCharacter(character);
            characterSelector.setItems(FXCollections.observableArrayList(repository.getCharacters()));
            characterSelector.getSelectionModel().select(character);
        } catch (SQLException exception) {
            showError("Could not save character", exception.getMessage());
        }
    }

    @FXML
    private void handleAddObjective() {
        String character = characterSelector.getValue();
        String world = worldSelector.getValue();
        String category = categorySelector.getValue();
        if (character == null) {
            showError("Add a character first", "Create or select a character before adding progress items.");
            return;
        }
        TextField title = new TextField();
        title.setPromptText("Quest, badge, or milestone");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add progression item");
        dialog.setHeaderText(character + " · " + world + " · " + category);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Name"), title);
        dialog.getDialogPane().setContent(form);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        if (title.getText().isBlank()) {
            showError("Missing item name", "Enter a quest, badge, or milestone name.");
            return;
        }
        try {
            repository.addProgressionItem(character, world, category, title.getText().trim());
            refreshObjectives();
        } catch (SQLException exception) {
            showError("Could not save progression item", exception.getMessage());
        }
    }

    private void updateCompletion(ProgressionItem item, boolean complete) {
        try {
            repository.updateProgressionItem(item.id(), complete);
            refreshObjectives();
        } catch (SQLException exception) {
            showError("Could not update completion", exception.getMessage());
            refreshObjectives();
        }
    }

    private void refreshObjectives() {
        String character = characterSelector.getValue();
        String world = worldSelector.getValue();
        if (character == null || world == null) {
            objectiveTable.getItems().clear();
            worldProgress.setProgress(0);
            progressSummary.setText("Add a character to start a world checklist.");
            return;
        }
        try {
            List<ProgressionItem> items = repository.getProgressionItems(character, world);
            objectiveTable.setItems(FXCollections.observableArrayList(items));
            long complete = items.stream().filter(ProgressionItem::complete).count();
            worldProgress.setProgress(items.isEmpty() ? 0 : (double) complete / items.size());
            progressSummary.setText(character + " · " + world + " · " + complete + " of "
                    + items.size() + " tracked milestones complete");
        } catch (SQLException exception) {
            showError("Could not load progression", exception.getMessage());
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
