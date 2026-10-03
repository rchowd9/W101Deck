package com.wizard101.deckbuilder.service;

import com.wizard101.deckbuilder.model.Card;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class PlannerRepository {
    private static final PlannerRepository INSTANCE = new PlannerRepository();
    private final String jdbcUrl;

    private PlannerRepository() {
        Path dataDirectory = Path.of(System.getProperty("user.home"), ".wizard101-deck-builder");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the local planner data directory.", exception);
        }
        jdbcUrl = "jdbc:sqlite:" + dataDirectory.resolve("planner.db");
        initialize();
    }

    public static PlannerRepository getInstance() {
        return INSTANCE;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private void initialize() {
        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS saved_decks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS saved_deck_cards (
                    deck_id INTEGER NOT NULL REFERENCES saved_decks(id) ON DELETE CASCADE,
                    position INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    school TEXT NOT NULL,
                    pip_cost INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    PRIMARY KEY (deck_id, position)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS craft_projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    character_name TEXT NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS craft_materials (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER NOT NULL REFERENCES craft_projects(id) ON DELETE CASCADE,
                    name TEXT NOT NULL,
                    source TEXT NOT NULL,
                    required_count INTEGER NOT NULL CHECK (required_count >= 0),
                    owned_count INTEGER NOT NULL CHECK (owned_count >= 0)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS characters (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS progression_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    character_name TEXT NOT NULL,
                    world TEXT NOT NULL,
                    category TEXT NOT NULL,
                    title TEXT NOT NULL,
                    complete INTEGER NOT NULL DEFAULT 0 CHECK (complete IN (0, 1))
                )
                """
        };
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialize the local planner database.", exception);
        }
    }

    public void saveDeck(String name, List<Card> cards) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                long deckId;
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO saved_decks(name) VALUES (?) "
                                + "ON CONFLICT(name) DO UPDATE SET name = excluded.name")) {
                    insert.setString(1, name);
                    insert.executeUpdate();
                }
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT id FROM saved_decks WHERE name = ?")) {
                    select.setString(1, name);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("The saved deck could not be found after writing it.");
                        }
                        deckId = result.getLong("id");
                    }
                }
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM saved_deck_cards WHERE deck_id = ?")) {
                    delete.setLong(1, deckId);
                    delete.executeUpdate();
                }
                try (PreparedStatement insertCard = connection.prepareStatement(
                        "INSERT INTO saved_deck_cards(deck_id, position, name, school, pip_cost, description) "
                                + "VALUES (?, ?, ?, ?, ?, ?)")) {
                    for (int index = 0; index < cards.size(); index++) {
                        Card card = cards.get(index);
                        insertCard.setLong(1, deckId);
                        insertCard.setInt(2, index);
                        insertCard.setString(3, card.getName());
                        insertCard.setString(4, card.getSchool());
                        insertCard.setInt(5, card.getPipCost());
                        insertCard.setString(6, card.getDescription());
                        insertCard.addBatch();
                    }
                    insertCard.executeBatch();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<String> getSavedDeckNames() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT name FROM saved_decks ORDER BY name")) {
            while (results.next()) {
                names.add(results.getString("name"));
            }
        }
        return names;
    }

    public List<Card> loadDeck(String name) throws SQLException {
        List<Card> cards = new ArrayList<>();
        String sql = """
                SELECT c.name, c.school, c.pip_cost, c.description
                FROM saved_deck_cards c
                JOIN saved_decks d ON d.id = c.deck_id
                WHERE d.name = ?
                ORDER BY c.position
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    cards.add(new Card(results.getString("name"), results.getString("school"),
                            results.getInt("pip_cost"), results.getString("description")));
                }
            }
        }
        return cards;
    }

    public long addCraftProject(String name, String characterName) throws SQLException {
        String sql = "INSERT INTO craft_projects(name, character_name) VALUES (?, ?)";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, characterName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The crafting project was created without an id.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<CraftProject> getCraftProjects() throws SQLException {
        List<CraftProject> projects = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT id, name, character_name FROM craft_projects ORDER BY character_name, name")) {
            while (results.next()) {
                projects.add(new CraftProject(results.getLong("id"), results.getString("name"),
                        results.getString("character_name")));
            }
        }
        return projects;
    }

    public void addCraftMaterial(long projectId, String name, String source,
                                 int requiredCount, int ownedCount) throws SQLException {
        String sql = """
                INSERT INTO craft_materials(project_id, name, source, required_count, owned_count)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            statement.setString(2, name);
            statement.setString(3, source);
            statement.setInt(4, requiredCount);
            statement.setInt(5, ownedCount);
            statement.executeUpdate();
        }
    }

    public List<CraftMaterial> getCraftMaterials(long projectId) throws SQLException {
        List<CraftMaterial> materials = new ArrayList<>();
        String sql = """
                SELECT id, name, source, required_count, owned_count
                FROM craft_materials WHERE project_id = ? ORDER BY name
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    materials.add(new CraftMaterial(results.getLong("id"), results.getString("name"),
                            results.getString("source"), results.getInt("required_count"),
                            results.getInt("owned_count")));
                }
            }
        }
        return materials;
    }

    public void updateCraftMaterialCount(long materialId, int ownedCount) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE craft_materials SET owned_count = ? WHERE id = ?")) {
            statement.setInt(1, ownedCount);
            statement.setLong(2, materialId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("The crafting material no longer exists.");
            }
        }
    }

    public void addCharacter(String name) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO characters(name) VALUES (?) ON CONFLICT(name) DO NOTHING")) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    public List<String> getCharacters() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT name FROM characters ORDER BY name")) {
            while (results.next()) {
                names.add(results.getString("name"));
            }
        }
        return names;
    }

    public void addProgressionItem(String characterName, String world, String category, String title)
            throws SQLException {
        String sql = """
                INSERT INTO progression_items(character_name, world, category, title)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, characterName);
            statement.setString(2, world);
            statement.setString(3, category);
            statement.setString(4, title);
            statement.executeUpdate();
        }
    }

    public List<ProgressionItem> getProgressionItems(String characterName, String world) throws SQLException {
        List<ProgressionItem> items = new ArrayList<>();
        String sql = """
                SELECT id, world, category, title, complete
                FROM progression_items
                WHERE character_name = ? AND world = ?
                ORDER BY CASE category WHEN 'Main quest' THEN 0 WHEN 'Side quest' THEN 1 ELSE 2 END, title
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, characterName);
            statement.setString(2, world);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    items.add(new ProgressionItem(results.getLong("id"), results.getString("world"),
                            results.getString("category"), results.getString("title"),
                            results.getBoolean("complete")));
                }
            }
        }
        return items;
    }

    public void updateProgressionItem(long itemId, boolean complete) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE progression_items SET complete = ? WHERE id = ?")) {
            statement.setBoolean(1, complete);
            statement.setLong(2, itemId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("The progression item no longer exists.");
            }
        }
    }

    public record CraftProject(long id, String name, String characterName) {
        @Override
        public String toString() {
            return characterName + " — " + name;
        }
    }

    public record CraftMaterial(long id, String name, String source, int requiredCount, int ownedCount) {
    }

    public record ProgressionItem(long id, String world, String category, String title, boolean complete) {
    }
}
