package com.wizard101.deckbuilder.model;

public class Card {
    private final String name;
    private final String school;
    private final int pipCost;
    private final String description;
    private final Role role;

    public Card(String name, String school, int pipCost, String description) {
        this(name, school, pipCost, description, Role.OTHER);
    }

    public Card(String name, String school, int pipCost, String description, Role role) {
        this.name = name;
        this.school = school;
        this.pipCost = pipCost;
        this.description = description;
        this.role = role;
    }

    public String getName() { return name; }
    public String getSchool() { return school; }
    public int getPipCost() { return pipCost; }
    public String getDescription() { return description; }
    public Role getRole() { return role; }

    public enum Role {
        AREA_OF_EFFECT("AoE"),
        DAMAGE_ENCHANT("Damage Enchant"),
        SINGLE_TARGET("Single-Target Hit"),
        FEINT("Feint"),
        HEALING("Healing"),
        SHIELD("Shield"),
        CLEANSE("Cleanse"),
        OTHER("Other");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}