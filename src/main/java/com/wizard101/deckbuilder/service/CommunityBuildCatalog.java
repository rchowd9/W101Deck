package com.wizard101.deckbuilder.service;

import java.util.List;

public final class CommunityBuildCatalog {
    private static final List<Build> BUILDS = List.of(
            new Build("Storm PvE AoE", "Storm", 160, "PvE AoE",
                    List.of("Tempest", "Glowbug Squall", "Storm Lord", "Colossal", "Feint"),
                    "Sample endgame area-damage setup."),
            new Build("Storm boss hitter", "Storm", 100, "Boss hitter",
                    List.of("Rusalka's Wrath", "Thunder Strike", "Colossal", "Epic", "Feint"),
                    "Sample single-target setup for boss encounters."),
            new Build("Life team support", "Life", 100, "Team support",
                    List.of("Satyr", "Tower Shield", "Cleanse Charm", "Feint"),
                    "Sample support setup with healing and protection."),
            new Build("Death solo utility", "Death", 60, "Solo utility",
                    List.of("Feint", "Sacrifice", "Tower Shield", "Cleanse Charm"),
                    "Sample solo setup with utility and recovery.")
    );

    private CommunityBuildCatalog() {
    }

    public static List<Build> getBuilds() {
        return BUILDS;
    }

    public record Build(String name, String school, int level, String role,
                        List<String> cardNames, String description) {
        @Override
        public String toString() {
            return name + " · Level " + level;
        }
    }
}
