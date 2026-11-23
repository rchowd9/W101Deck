package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.CombatCalculator;
import com.wizard101.deckbuilder.service.CombatCalculator.Combo;
import com.wizard101.deckbuilder.service.CombatCalculator.Inputs;
import com.wizard101.deckbuilder.service.CombatCalculator.Result;
import com.wizard101.deckbuilder.service.CombatSimulation;
import com.wizard101.deckbuilder.service.CombatSimulation.Mode;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;

public class CombatPlannerController {
    @FXML private TextField wizardDamage;
    @FXML private TextField wizardPierce;
    @FXML private TextField wizardResist;
    @FXML private TextField wizardAccuracy;
    @FXML private TextField targetHealth;
    @FXML private TextField targetResist;
    @FXML private TextField spellBaseDamage;
    @FXML private TextField spellBaseAccuracy;
    @FXML private TextField bladeBoost;
    @FXML private TextField trapBoost;
    @FXML private TextField incomingRawDamage;
    @FXML private ComboBox<String> combatMode;
    @FXML private TextField deckSize;
    @FXML private TextField attackCardCount;
    @FXML private TextField utilityCardCount;
    @FXML private TextField openingHandSize;
    @FXML private Label calculationSummary;
    @FXML private Label comboBreakdown;
    @FXML private Label simulationSummary;

    @FXML
    public void initialize() {
        List.of(wizardDamage, wizardPierce, wizardResist, wizardAccuracy, targetHealth,
                        targetResist, spellBaseDamage, spellBaseAccuracy, bladeBoost, trapBoost,
                        incomingRawDamage, deckSize, attackCardCount, utilityCardCount,
                        openingHandSize)
                .forEach(field -> field.textProperty().addListener(
                        (observable, oldValue, newValue) -> recalculate()));
        combatMode.getItems().setAll("PvE", "PvP");
        combatMode.getSelectionModel().selectFirst();
        combatMode.valueProperty().addListener(
                (observable, oldValue, newValue) -> recalculate());
        recalculate();
    }

    private void recalculate() {
        try {
            Inputs inputs = new Inputs(
                    number(wizardDamage),
                    number(wizardPierce),
                    number(wizardResist),
                    number(wizardAccuracy),
                    number(targetHealth),
                    number(targetResist),
                    number(spellBaseDamage),
                    number(spellBaseAccuracy),
                    number(bladeBoost),
                    number(trapBoost),
                    optionalNumber(incomingRawDamage));
            Result result = CombatCalculator.calculate(inputs);
            showResult(result);
            showSimulation(result);
        } catch (IllegalArgumentException exception) {
            calculationSummary.setText(exception.getMessage());
            comboBreakdown.setText("");
            simulationSummary.setText("");
        }
    }

    private void showSimulation(Result combatResult) {
        Mode mode = "PvP".equals(combatMode.getValue()) ? Mode.PVP : Mode.PVE;
        double baseHitDamage = combatResult.combos().get(0).estimatedDamage();
        CombatSimulation.Result simulation = CombatSimulation.simulate(
                new CombatSimulation.Inputs(
                        mode,
                        integer(deckSize),
                        integer(attackCardCount),
                        integer(utilityCardCount),
                        integer(openingHandSize),
                        baseHitDamage,
                        combatResult.hitChancePercent(),
                        number(targetHealth)));
        simulationSummary.setText(String.format(Locale.ROOT,
                "%s opening-hand estimate: %.1f%% chance to draw an attack, "
                        + "%.1f%% chance to draw utility, %.1f%% chance to draw a useful card. "
                        + "Expected damage: %.0f; chance to defeat this target with one hit: %.1f%%.",
                mode == Mode.PVE ? "PvE (attack-focused)" : "PvP (attack or utility)",
                simulation.attackDrawChance() * 100,
                simulation.utilityDrawChance() * 100,
                simulation.usefulDrawChance() * 100,
                simulation.expectedOpeningDamage(),
                simulation.killChance() * 100));
    }

    private void showResult(Result result) {
        Combo recommended = result.recommendedCombo();
        if (recommended == null) {
            String needed = Double.isFinite(result.additionalBoostNeededPercent())
                    ? String.format(Locale.ROOT, "%.1f%% total combined boost",
                            result.additionalBoostNeededPercent())
                    : "pierce or another damage source is needed to overcome target resist";
            calculationSummary.setText("No configured combo reaches the target's health. "
                    + "The unboosted hit would need " + needed + " to reach it.");
        } else {
            calculationSummary.setText("Most efficient configured combo: "
                    + recommended.name() + " for about "
                    + formatDamage(recommended.estimatedDamage()) + " damage.");
        }

        StringBuilder breakdown = new StringBuilder();
        for (Combo combo : result.combos()) {
            if (!breakdown.isEmpty()) {
                breakdown.append('\n');
            }
            breakdown.append(combo.name()).append(": ")
                    .append(formatDamage(combo.estimatedDamage())).append(" damage");
        }
        breakdown.append(String.format(Locale.ROOT, "%nEstimated hit chance: %.1f%%",
                result.hitChancePercent()));
        if (result.estimatedIncomingDamage() != null) {
            breakdown.append(String.format(Locale.ROOT, "%nIncoming hit after your resist: %.0f",
                    result.estimatedIncomingDamage()));
        }
        comboBreakdown.setText(breakdown.toString());
    }

    private static double number(TextField field) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Fill in all required stats to calculate a combo.");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number for " + field.getPromptText() + ".");
        }
    }

    private static Double optionalNumber(TextField field) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            return null;
        }

        private static int integer(TextField field) {
            String value = field.getText().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Enter a whole number for " + field.getPromptText() + ".");
            }
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number for " + field.getPromptText() + ".");
        }
    }

    private static String formatDamage(double damage) {
        return String.format(Locale.ROOT, "%.0f", damage);
    }
}
