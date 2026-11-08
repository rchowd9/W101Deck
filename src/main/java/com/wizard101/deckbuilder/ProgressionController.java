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
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProgressionController {
    private static final List<String> SCHOOLS = List.of(
            "Fire", "Ice", "Storm", "Myth", "Life", "Death", "Balance");
    private static final Map<String, String> SCHOOL_ICONS = Map.of(
            "Fire", "🔥",
            "Ice", "❄",
            "Storm", "⚡",
            "Myth", "🗿",
            "Life", "🌿",
            "Death", "💀",
            "Balance", "⚖");
    private static final Map<String, SchoolAdvice> SCHOOL_ADVICE = Map.of(
            "Fire", new SchoolAdvice(
                    "Death for Feint's offensive setup; Ice for a zero-pip defensive option.",
                    "Feint (Death) to amplify a target hit; Tower Shield (Ice) to reduce an incoming hit; "
                            + "Colossal or Epic (Sun) to strengthen a damage spell."),
            "Ice", new SchoolAdvice(
                    "Death for stronger offensive setup; Life for an optional team-healing tool.",
                    "Feint (Death) to amplify a target hit; Satyr (Life) for a substantial ally heal; "
                            + "Colossal or Epic (Sun) to strengthen a damage spell."),
            "Storm", new SchoolAdvice(
                    "Death for Feint's offensive setup, Life for healing, or Ice for extra protection.",
                    "Feint (Death) to amplify a target hit; Satyr (Life) for an optional ally heal; "
                            + "Tower Shield (Ice) for a zero-pip defense; Colossal or Epic (Sun) "
                            + "to strengthen a damage spell."),
            "Myth", new SchoolAdvice(
                    "Death for stronger offensive setup; Life for an optional team-healing tool.",
                    "Feint (Death) to amplify a target hit; Satyr (Life) for a substantial ally heal; "
                            + "Colossal or Epic (Sun) to strengthen a damage spell."),
            "Life", new SchoolAdvice(
                    "Death for stronger offensive setup; Ice for extra protection when healing is not enough.",
                    "Feint (Death) to amplify a target hit; Tower Shield (Ice) for a zero-pip defense; "
                            + "Colossal or Epic (Sun) to strengthen a damage spell."),
            "Death", new SchoolAdvice(
                    "Life for an optional ally-healing tool; Ice for extra protection.",
                    "Satyr (Life) for a substantial ally heal; Tower Shield (Ice) for a zero-pip defense; "
                            + "Colossal or Epic (Sun) to strengthen a damage spell."),
            "Balance", new SchoolAdvice(
                    "Death for stronger offensive setup, Life for healing, or Ice for extra protection.",
                    "Feint (Death) to amplify a target hit; Satyr (Life) for an optional ally heal; "
                            + "Tower Shield (Ice) for a zero-pip defense; Colossal or Epic (Sun) "
                            + "to strengthen a damage spell."));

    private static final List<String> WORLDS = List.of(
            "Wizard City", "Krokotopia", "Marleybone", "Mooshu", "Dragonspyre",
            "Celestia", "Zafaria", "Avalon", "Azteca", "Khrysalis", "Polaris",
            "Mirage", "Empyrea", "Karamelle", "Lemuria", "Novus", "Wallaru");

    @FXML private ComboBox<String> characterSelector;
    @FXML private ComboBox<String> worldSelector;
    @FXML private ComboBox<String> categorySelector;
    @FXML private ComboBox<String> schoolSelector;
    @FXML private Label secondarySchoolAdvice;
    @FXML private Label trainingSpellAdvice;
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
        schoolSelector.getItems().setAll(SCHOOLS);
        schoolSelector.setCellFactory(list -> new SchoolCell());
        schoolSelector.setButtonCell(new SchoolCell());
        schoolSelector.getSelectionModel().selectFirst();
        schoolSelector.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateSchoolAdvice());
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
        updateSchoolAdvice();
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

    private void updateSchoolAdvice() {
        SchoolAdvice advice = SCHOOL_ADVICE.get(schoolSelector.getValue());
        if (advice == null) {
            secondarySchoolAdvice.setText("");
            trainingSpellAdvice.setText("");
            return;
        }
        secondarySchoolAdvice.setText(advice.secondarySchools());
        trainingSpellAdvice.setText(advice.trainingSpells());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "An unexpected database error occurred." : message);
        alert.showAndWait();
    }

    private record SchoolAdvice(String secondarySchools, String trainingSpells) {
    }

    private static final class SchoolCell extends ListCell<String> {
        @Override
        protected void updateItem(String school, boolean empty) {
            super.updateItem(school, empty);
            if (empty || school == null) {
                setText(null);
                setGraphic(null);
                setAccessibleText(null);
                return;
            }
            Label icon = new Label(SCHOOL_ICONS.get(school));
            icon.getStyleClass().add("school-icon");
            setText(school);
            setGraphic(icon);
            setAccessibleText(school + " school");
        }
    }
}
