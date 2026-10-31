package com.wizard101.deckbuilder.service;

import com.wizard101.deckbuilder.model.Card;
import java.util.ArrayList;
import java.util.List;

public class CardDatabase {
    private final List<Card> spellCatalog = new ArrayList<>();

    public CardDatabase() {
        // Sample Wizard101 Spells with a wider mix of language-inspired deck options.
        spellCatalog.add(new Card("Fire Cat", "Fire", 1, "80-120 Fire Damage"));
        spellCatalog.add(new Card("Heckhound", "Fire", 1, "130 Fire Damage per Pip over 3 turns"));
        spellCatalog.add(new Card("Java Jolt", "Fire", 2, "220 Fire Damage with a clean compile"));
        spellCatalog.add(new Card("Python Pulse", "Storm", 2, "190 Storm Damage and fast iteration"));
        spellCatalog.add(new Card("TypeScript Tempest", "Storm", 3, "360 Storm Damage with strong typing"));
        spellCatalog.add(new Card("C# Cascade", "Ice", 2, "200 Ice Damage and smooth control flow"));
        spellCatalog.add(new Card("Go Guardian", "Life", 3, "420 Health to an ally and dependable scaling"));
        spellCatalog.add(new Card("Rust Rend", "Death", 4, "670 Death Damage with a sharp edge"));
        spellCatalog.add(new Card("Ruby Renewal", "Life", 2, "300 Health restored and easy to maintain"));
        spellCatalog.add(new Card("Tower Shield", "Ice", 0, "-50% to next damage spell"));
        spellCatalog.add(new Card("Satyr", "Life", 4, "860 Health to target"));
    }

    public List<Card> getAllSpells() {
        return spellCatalog;
    }
}