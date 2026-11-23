package com.wizard101.deckbuilder.service;

public final class CombatSimulation {
    private CombatSimulation() {
    }

    public static Result simulate(Inputs inputs) {
        validate(inputs);
        double attackDrawChance = drawChance(
                inputs.deckSize(), inputs.attackCardCount(), inputs.openingHandSize());
        double utilityDrawChance = drawChance(
                inputs.deckSize(), inputs.utilityCardCount(), inputs.openingHandSize());
        double usefulDrawChance = inputs.mode() == Mode.PVE
                ? attackDrawChance
                : drawChance(inputs.deckSize(),
                        inputs.attackCardCount() + inputs.utilityCardCount(),
                        inputs.openingHandSize());
        double landedHitChance = attackDrawChance * inputs.hitChancePercent() / 100;
        double expectedDamage = landedHitChance
                * Math.min(inputs.damagePerHit(), inputs.targetHealth());
        double killChance = inputs.damagePerHit() >= inputs.targetHealth()
                ? landedHitChance : 0;
        return new Result(attackDrawChance, utilityDrawChance, usefulDrawChance,
                expectedDamage, killChance);
    }

    private static double drawChance(int deckSize, int cardCount, int handSize) {
        double noCardChance = 1;
        for (int draw = 0; draw < handSize; draw++) {
            noCardChance *= (double) (deckSize - cardCount - draw) / (deckSize - draw);
        }
        return 1 - noCardChance;
    }

    private static void validate(Inputs inputs) {
        if (inputs == null || inputs.mode() == null) {
            throw new IllegalArgumentException("Choose a combat mode and enter deck details.");
        }
        if (inputs.deckSize() < 1) {
            throw new IllegalArgumentException("Deck size must be at least 1.");
        }
        if (inputs.attackCardCount() < 0 || inputs.utilityCardCount() < 0
                || inputs.attackCardCount() + inputs.utilityCardCount() > inputs.deckSize()) {
            throw new IllegalArgumentException(
                    "Attack and utility card counts must be non-negative and fit in the deck.");
        }
        if (inputs.openingHandSize() < 1 || inputs.openingHandSize() > inputs.deckSize()) {
            throw new IllegalArgumentException(
                    "Opening hand size must be between 1 and the deck size.");
        }
        if (!Double.isFinite(inputs.damagePerHit()) || inputs.damagePerHit() < 0) {
            throw new IllegalArgumentException("Estimated hit damage must be zero or greater.");
        }
        if (!Double.isFinite(inputs.hitChancePercent())
                || inputs.hitChancePercent() < 0 || inputs.hitChancePercent() > 100) {
            throw new IllegalArgumentException("Hit chance must be between 0 and 100%.");
        }
        if (!Double.isFinite(inputs.targetHealth()) || inputs.targetHealth() <= 0) {
            throw new IllegalArgumentException("Target health must be greater than zero.");
        }
    }

    public enum Mode {
        PVE,
        PVP
    }

    public record Inputs(
            Mode mode,
            int deckSize,
            int attackCardCount,
            int utilityCardCount,
            int openingHandSize,
            double damagePerHit,
            double hitChancePercent,
            double targetHealth) {
    }

    public record Result(
            double attackDrawChance,
            double utilityDrawChance,
            double usefulDrawChance,
            double expectedOpeningDamage,
            double killChance) {
    }
}
