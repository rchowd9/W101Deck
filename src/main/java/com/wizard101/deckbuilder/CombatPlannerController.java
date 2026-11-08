package com.wizard101.deckbuilder;

import com.wizard101.deckbuilder.service.CombatCalculator;
import com.wizard101.deckbuilder.service.CombatCalculator.Combo;
import com.wizard101.deckbuilder.service.CombatCalculator.Inputs;
import com.wizard101.deckbuilder.service.CombatCalculator.Result;
import javafx.fxml.FXML;
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
    @FXML private Label calculationSummary;
    @FXML private Label comboBreakdown;

    @FXML
    public void initialize() {
        List.of(wizardDamage, wizardPierce, wizardResist, wizardAccuracy, targetHealth,
                        targetResist, spellBaseDamage, spellBaseAccuracy, bladeBoost, trapBoost,
                        incomingRawDamage)
                .forEach(field -> field.textProperty().addListener(
                        (observable, oldValue, newValue) -> recalculate()));
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
        } catch (IllegalArgumentException exception) {
            calculationSummary.setText(exception.getMessage());
            comboBreakdown.setText("");
        }
    }

    private void showResult(Result result) {
        Combo recommended = result.recommendedCombo();
        if (recommended == null) {
            String needed = Double.isFinite(result.additionalBoostNeededPercent())
                    ? String.format(Locale.ROOT, "%.1f%% additional combined boost",
                            result.additionalBoostNeededPercent())
                    : "pierce or another damage source is needed to overcome target resist";
            calculationSummary.setText("No configured combo reaches the target's health; "
                    + needed + " beyond the unboosted hit.");
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
