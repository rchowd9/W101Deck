package com.wizard101.deckbuilder.service;

import java.util.List;

public final class CombatCalculator {
    private CombatCalculator() {
    }

    public static Result calculate(Inputs inputs) {
        validate(inputs);
        double effectiveResist = Math.max(0,
                inputs.targetResistPercent() - inputs.wizardPiercePercent());
        double unboostedDamage = inputs.spellBaseDamage()
                * (1 + inputs.wizardDamagePercent() / 100)
                * (1 - effectiveResist / 100);
        double bladeMultiplier = 1 + inputs.bladePercent() / 100;
        double trapMultiplier = 1 + inputs.trapPercent() / 100;

        List<Combo> combos = List.of(
                new Combo("Hit only", 0, damage(unboostedDamage)),
                new Combo("Blade + hit", 1, damage(unboostedDamage * bladeMultiplier)),
                new Combo("Trap + hit", 1, damage(unboostedDamage * trapMultiplier)),
                new Combo("Blade + trap + hit", 2,
                        damage(unboostedDamage * bladeMultiplier * trapMultiplier)));
        Combo recommended = combos.stream()
                .filter(combo -> combo.estimatedDamage() >= inputs.targetHealth())
                .min((first, second) -> {
                    int setupComparison = Integer.compare(first.setupSpells(), second.setupSpells());
                    return setupComparison != 0 ? setupComparison
                            : Double.compare(first.estimatedDamage(), second.estimatedDamage());
                })
                .orElse(null);
        double additionalBoost = unboostedDamage <= 0
                ? Double.POSITIVE_INFINITY
                : Math.max(0, (inputs.targetHealth() / unboostedDamage - 1) * 100);
        Double incomingDamage = inputs.incomingRawDamage() == null
                ? null
                : damage(inputs.incomingRawDamage() * (1 - inputs.wizardResistPercent() / 100));
        double hitChance = Math.min(100,
                inputs.spellBaseAccuracyPercent() + inputs.wizardAccuracyPercent());

        return new Result(combos, recommended, hitChance, incomingDamage, additionalBoost);
    }

    private static double damage(double value) {
        return Math.floor(value);
    }

    private static void validate(Inputs inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("Enter the wizard, spell, and target stats.");
        }
        percentage("Wizard damage", inputs.wizardDamagePercent(), 500);
        percentage("Wizard pierce", inputs.wizardPiercePercent(), 100);
        percentage("Wizard resist", inputs.wizardResistPercent(), 100);
        percentage("Wizard accuracy", inputs.wizardAccuracyPercent(), 100);
        positive("Target health", inputs.targetHealth());
        percentage("Target resist", inputs.targetResistPercent(), 100);
        positive("Spell base damage", inputs.spellBaseDamage());
        percentage("Spell base accuracy", inputs.spellBaseAccuracyPercent(), 100);
        percentage("Blade boost", inputs.bladePercent(), 200);
        percentage("Trap boost", inputs.trapPercent(), 200);
        if (inputs.incomingRawDamage() != null
                && (!Double.isFinite(inputs.incomingRawDamage()) || inputs.incomingRawDamage() < 0)) {
            throw new IllegalArgumentException("Incoming hit damage must be zero or greater.");
        }
    }

    private static void percentage(String name, double value, double maximum) {
        if (!Double.isFinite(value) || value < 0 || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between 0 and " + (int) maximum + "%.");
        }
    }

    private static void positive(String name, double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero.");
        }
    }

    public record Inputs(
            double wizardDamagePercent,
            double wizardPiercePercent,
            double wizardResistPercent,
            double wizardAccuracyPercent,
            double targetHealth,
            double targetResistPercent,
            double spellBaseDamage,
            double spellBaseAccuracyPercent,
            double bladePercent,
            double trapPercent,
            Double incomingRawDamage) {
    }

    public record Combo(String name, int setupSpells, double estimatedDamage) {
    }

    public record Result(
            List<Combo> combos,
            Combo recommendedCombo,
            double hitChancePercent,
            Double estimatedIncomingDamage,
            double additionalBoostNeededPercent) {
    }
}
