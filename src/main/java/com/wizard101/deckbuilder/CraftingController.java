package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.PlannerRepository;
import com.wizard101.deckbuilder.service.PlannerRepository.CraftMaterial;
import com.wizard101.deckbuilder.service.PlannerRepository.CraftProject;
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
import javafx.scene.control.TextFieldTableCell;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CraftingController {
    @FXML private ComboBox<CraftProject> projectSelector;
    @FXML private TableView<CraftMaterial> materialTable;
    @FXML private TableColumn<CraftMaterial, String> materialColumn;
    @FXML private TableColumn<CraftMaterial, String> sourceColumn;
    @FXML private TableColumn<CraftMaterial, String> requiredColumn;
    @FXML private TableColumn<CraftMaterial, String> ownedColumn;
    @FXML private TableColumn<CraftMaterial, String> missingColumn;
    @FXML private Label projectSummary;
    @FXML private Label missingAlert;
    @FXML private ProgressBar materialProgress;

    private final PlannerRepository repository = PlannerRepository.getInstance();

    @FXML
    public void initialize() {
        materialColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        sourceColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().source()));
        requiredColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(cell.getValue().requiredCount())));
        ownedColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(cell.getValue().ownedCount())));
        missingColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(Integer.toString(Math.max(
                        0, cell.getValue().requiredCount() - cell.getValue().ownedCount()))));
        ownedColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ownedColumn.setOnEditCommit(event -> updateOwnedCount(event.getRowValue(), event.getNewValue()));
        projectSelector.valueProperty().addListener((observable, oldValue, newValue) -> refreshMaterials());
        refreshProjects();
    }

    @FXML
    private void handleAddProject() {
        TextField name = new TextField();
        name.setPromptText("e.g. Dragoon set, Aeon wand, Novus robe");
        TextField character = new TextField();
        character.setPromptText("Character name");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New crafting goal");
        dialog.setHeaderText("Create a gear or crafting project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Goal"), name);
        form.addRow(1, new Label("Character"), character);
        dialog.getDialogPane().setContent(form);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        if (name.getText().isBlank() || character.getText().isBlank()) {
            showError("Missing project details", "Enter both a project goal and a character name.");
            return;
        }
        try {
            repository.addCraftProject(name.getText().trim(), character.getText().trim());
            refreshProjects();
            projectSelector.getItems().stream()
                    .filter(project -> project.name().equals(name.getText().trim())
                            && project.characterName().equals(character.getText().trim()))
                    .findFirst().ifPresent(projectSelector.getSelectionModel()::select);
        } catch (SQLException exception) {
            showError("Could not create project", exception.getMessage());
        }
    }

    @FXML
    private void handleAddMaterial() {
        CraftProject project = projectSelector.getValue();
        if (project == null) {
            showError("Choose a project", "Create or select a crafting goal before adding materials.");
            return;
        }
        TextField name = new TextField();
        name.setPromptText("Reagent, boss drop, or other item");
        TextField source = new TextField();
        source.setPromptText("Boss/world/vendor and location");
        TextField required = new TextField("1");
        TextField owned = new TextField("0");
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add crafting material");
        dialog.setHeaderText("Track a required reagent or drop");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Material"), name);
        form.addRow(1, new Label("Source"), source);
        form.addRow(2, new Label("Required"), required);
        form.addRow(3, new Label("Owned"), owned);
        dialog.getDialogPane().setContent(form);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            if (name.getText().isBlank() || source.getText().isBlank()) {
                throw new IllegalArgumentException("Enter a material name and where it comes from.");
            }
            int requiredCount = parseCount(required.getText(), "Required");
            int ownedCount = parseCount(owned.getText(), "Owned");
            repository.addCraftMaterial(project.id(), name.getText().trim(), source.getText().trim(),
                    requiredCount, ownedCount);
            refreshMaterials();
        } catch (IllegalArgumentException exception) {
            showError("Invalid material", exception.getMessage());
        } catch (SQLException exception) {
            showError("Could not add material", exception.getMessage());
        }
    }

    private void updateOwnedCount(CraftMaterial material, String value) {
        try {
            repository.updateCraftMaterialCount(material.id(), parseCount(value, "Owned"));
            refreshMaterials();
        } catch (IllegalArgumentException exception) {
            showError("Invalid inventory count", exception.getMessage());
            refreshMaterials();
        } catch (SQLException exception) {
            showError("Could not save inventory", exception.getMessage());
            refreshMaterials();
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

    private void refreshProjects() {
        try {
            CraftProject selected = projectSelector.getValue();
            List<CraftProject> projects = repository.getCraftProjects();
            projectSelector.setItems(FXCollections.observableArrayList(projects));
            if (selected != null) {
                projects.stream().filter(project -> project.id() == selected.id())
                        .findFirst().ifPresent(projectSelector.getSelectionModel()::select);
            }
            if (projectSelector.getValue() == null && !projects.isEmpty()) {
                projectSelector.getSelectionModel().selectFirst();
            }
            if (projects.isEmpty()) {
                materialTable.getItems().clear();
                projectSummary.setText("Create a goal, then add the recipe or materials you still need.");
                missingAlert.setText("No crafting goal selected.");
                materialProgress.setProgress(0);
            }
        } catch (SQLException exception) {
            showError("Could not load crafting goals", exception.getMessage());
        }
    }

    private void refreshMaterials() {
        CraftProject project = projectSelector.getValue();
        if (project == null) {
            return;
        }
        try {
            List<CraftMaterial> materials = repository.getCraftMaterials(project.id());
            materialTable.setItems(FXCollections.observableArrayList(materials));
            int required = materials.stream().mapToInt(CraftMaterial::requiredCount).sum();
            int owned = materials.stream().mapToInt(material ->
                    Math.min(material.requiredCount(), material.ownedCount())).sum();
            int missingKinds = (int) materials.stream().filter(material ->
                    material.ownedCount() < material.requiredCount()).count();
            projectSummary.setText(project.characterName() + " — " + project.name()
                    + " · " + materials.size() + " tracked materials · " + owned + " / " + required + " collected");
            missingAlert.setText(missingKinds == 0
                    ? (materials.isEmpty() ? "Add recipe materials to begin tracking." : "All tracked requirements met.")
                    : missingKinds + " material type(s) still missing. Edit the Owned column as inventory changes.");
            materialProgress.setProgress(required == 0 ? (materials.isEmpty() ? 0 : 1) : (double) owned / required);
        } catch (SQLException exception) {
            showError("Could not load crafting materials", exception.getMessage());
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
