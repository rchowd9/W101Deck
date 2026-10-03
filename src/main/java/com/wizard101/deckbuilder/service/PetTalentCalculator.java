package com.wizard101.deckbuilder.service;

import java.util.Map;
import java.util.TreeSet;

public final class PetTalentCalculator {
    private PetTalentCalculator() {
    }

    public static double offspringManifestationChance(double parentAChance, double parentBChance) {
        if (!Double.isFinite(parentAChance) || !Double.isFinite(parentBChance)
                || parentAChance < 0 || parentAChance > 100 || parentBChance < 0 || parentBChance > 100) {
            throw new IllegalArgumentException("Talent pool percentages must be between 0 and 100.");
        }
        double chanceA = parentAChance / 100.0;
        double chanceB = parentBChance / 100.0;
        return (1.0 - (1.0 - chanceA) * (1.0 - chanceB)) * 100.0;
    }

    public static Map<String, Double> calculate(Map<String, Double> parentA, Map<String, Double> parentB) {
        TreeSet<String> talents = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        talents.addAll(parentA.keySet());
        talents.addAll(parentB.keySet());
        java.util.TreeMap<String, Double> results = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String talent : talents) {
            results.put(talent, offspringManifestationChance(
                    parentA.getOrDefault(talent, 0.0), parentB.getOrDefault(talent, 0.0)));
        }
        return results;
    }
}
