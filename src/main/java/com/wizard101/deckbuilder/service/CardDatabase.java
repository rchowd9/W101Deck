package com.wizard101.deckbuilder.service;

import com.wizard101.deckbuilder.model.Card;
import java.util.ArrayList;
import java.util.List;

public class CardDatabase {
    private final List<Card> spellCatalog = new ArrayList<>();

    public CardDatabase() {
        spellCatalog.add(new Card("Tempest", "Storm", 3,
                "Area damage that scales with pips spent.", Card.Role.AREA_OF_EFFECT));
        spellCatalog.add(new Card("Glowbug Squall", "Storm", 7,
                "Storm damage to all enemies.", Card.Role.AREA_OF_EFFECT));
        spellCatalog.add(new Card("Storm Lord", "Storm", 7,
                "Storm damage to all enemies.", Card.Role.AREA_OF_EFFECT));
        spellCatalog.add(new Card("Rusalka's Wrath", "Storm", 6,
                "A strong Storm hit for a single target.", Card.Role.SINGLE_TARGET));
        spellCatalog.add(new Card("Thunder Strike", "Storm", 4,
                "A direct Storm hit for a single target.", Card.Role.SINGLE_TARGET));
        spellCatalog.add(new Card("Fist of the Sun", "Sun", 4,
                "A direct hit for a single target.", Card.Role.SINGLE_TARGET));
        spellCatalog.add(new Card("Colossal", "Sun", 0,
                "Damage enchantment for scaling a hit.", Card.Role.DAMAGE_ENCHANT));
        spellCatalog.add(new Card("Epic", "Sun", 0,
                "Damage enchantment for scaling a hit.", Card.Role.DAMAGE_ENCHANT));
        spellCatalog.add(new Card("Feint", "Death", 3,
                "Trap that increases damage dealt to the target.", Card.Role.FEINT));
        spellCatalog.add(new Card("Satyr", "Life", 4,
                "Restore health to an ally.", Card.Role.HEALING));
        spellCatalog.add(new Card("Sacrifice", "Death", 1,
                "Restore health to an ally at a cost to the caster.", Card.Role.HEALING));
        spellCatalog.add(new Card("Tower Shield", "Ice", 0,
                "Reduce damage from the next incoming attack.", Card.Role.SHIELD));
        spellCatalog.add(new Card("Cleanse Charm", "Balance", 0,
                "Remove a negative charm from an ally.", Card.Role.CLEANSE));
    }

    public List<Card> getAllSpells() {
        return spellCatalog;
    }
}