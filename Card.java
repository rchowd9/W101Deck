package com.wizard101.deckbuilder.model;

public class Card {
    private final String name;
    private final String school;
    private final int pipCost;
    private final String description;

    public Card(String name, String school, int pipCost, String description) {
        this.name = name;
        this.school = school;
        this.pipCost = pipCost;
        this.description = description;
    }

    public String getName() { return name; }
    public String getSchool() { return school; }
    public int getPipCost() { return pipCost; }
    public String getDescription() { return description; }
}