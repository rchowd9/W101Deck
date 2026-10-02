package com.wizard101.deckbuilder.service;

import com.wizard101.deckbuilder.model.Card;
import java.util.ArrayList;
import java.util.List;

public class CardDatabase {
    private final List<Card> spellCatalog = new ArrayList<>();

    public CardDatabase() {
        // Sample Wizard101 Spells
        spellCatalog.add(new Card("Fire Cat", "Fire", 1, "80-120 Fire Damage"));
        spellCatalog.add(new Card("Heckhound", "Fire", 1, "130 Fire Damage per Pip over 3 turns"));
        spellCatalog.add(new Card("Storm Shark", "Storm", 3, "375-435 Storm Damage"));
        spellCatalog.add(new Card("Tower Shield", "Ice", 0, "-50% to next damage spell"));
        spellCatalog.add(new Card("Satyr", "Life", 4, "860 Health to target"));
    }

    public List<Card> getAllSpells() {
        return spellCatalog;
    }
}