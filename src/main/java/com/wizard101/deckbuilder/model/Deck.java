package com.wizard101.deckbuilder.model;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private final int capacity;
    private final List<Card> cards;

    public Deck(int capacity) {
        this.capacity = capacity;
        this.cards = new ArrayList<>();
    }

    public boolean addCard(Card card) {
        if (cards.size() < capacity) {
            cards.add(card);
            return true;
        }
        return false;
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    public void clear() {
        cards.clear();
    }

    public List<Card> getCards() { return cards; }
    public int getSize() { return cards.size(); }
    public int getCapacity() { return capacity; }

    public double getAveragePipCost() {
        if (cards.isEmpty()) return 0.0;
        int totalPips = cards.stream().mapToInt(Card::getPipCost).sum();
        return (double) totalPips / cards.size();
    }
}