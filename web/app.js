const MAX_DECK_SIZE = 64;
const SAVED_DECK_KEY = "wizard101-deck-builder-workspace";
const LEGACY_DECK_KEY = "wizard101-deck-builder";

const spells = [
  { id: "fire-cat", name: "Fire Cat", school: "Fire", rank: 1, type: "Damage", pipCost: 1, description: "Single-target Fire damage." },
  { id: "heckhound", name: "Heckhound", school: "Fire", rank: 3, type: "Damage", pipCost: 1, description: "Damage over time; its total damage scales with the pips invested." },
  { id: "meteor-strike", name: "Meteor Strike", school: "Fire", rank: 4, type: "Damage", pipCost: 4, description: "Fire damage to all enemies." },
  { id: "fire-dragon", name: "Fire Dragon", school: "Fire", rank: 7, type: "Damage", pipCost: 7, description: "Heavy Fire damage to all enemies, followed by damage over time." },
  { id: "rain-of-fire", name: "Rain of Fire", school: "Fire", rank: 9, type: "Damage", pipCost: 9, description: "Powerful Fire damage to all enemies with a damage-over-time effect." },
  { id: "frost-beetle", name: "Frost Beetle", school: "Ice", rank: 1, type: "Damage", pipCost: 1, description: "Single-target Ice damage." },
  { id: "tower-shield", name: "Tower Shield", school: "Ice", rank: 2, type: "Defense", pipCost: 0, description: "Reduce the next incoming damage from any school." },
  { id: "ice-colossus", name: "Ice Colossus", school: "Ice", rank: 5, type: "Damage", pipCost: 5, description: "Ice damage to one enemy and weaken its next attack." },
  { id: "blizzard", name: "Blizzard", school: "Ice", rank: 6, type: "Damage", pipCost: 6, description: "Ice damage to all enemies." },
  { id: "frost-giant", name: "Frost Giant", school: "Ice", rank: 7, type: "Damage", pipCost: 7, description: "Ice damage to all enemies and stun them." },
  { id: "thunder-snake", name: "Thunder Snake", school: "Storm", rank: 1, type: "Damage", pipCost: 1, description: "Single-target Storm damage." },
  { id: "storm-shark", name: "Storm Shark", school: "Storm", rank: 3, type: "Damage", pipCost: 3, description: "Strong single-target Storm damage." },
  { id: "tempest", name: "Tempest", school: "Storm", rank: 4, type: "Damage", pipCost: 4, description: "Storm damage to all enemies; damage increases with pips invested." },
  { id: "storm-lord", name: "Storm Lord", school: "Storm", rank: 7, type: "Damage", pipCost: 7, description: "Storm damage to all enemies and stun them." },
  { id: "blood-bat", name: "Blood Bat", school: "Myth", rank: 1, type: "Damage", pipCost: 1, description: "Single-target Myth damage." },
  { id: "humongofrog", name: "Humongofrog", school: "Myth", rank: 4, type: "Damage", pipCost: 4, description: "Myth damage to all enemies." },
  { id: "orthus", name: "Orthrus", school: "Myth", rank: 7, type: "Damage", pipCost: 7, description: "Powerful Myth damage to one enemy." },
  { id: "medusa", name: "Medusa", school: "Myth", rank: 8, type: "Damage", pipCost: 8, description: "Heavy Myth damage and a stun against one enemy." },
  { id: "imp", name: "Imp", school: "Life", rank: 1, type: "Healing", pipCost: 1, description: "Small heal to one ally." },
  { id: "satyr", name: "Satyr", school: "Life", rank: 4, type: "Healing", pipCost: 4, description: "Large heal to one ally." },
  { id: "forest-lord", name: "Forest Lord", school: "Life", rank: 8, type: "Damage", pipCost: 8, description: "Life damage to all enemies." },
  { id: "rebirth", name: "Rebirth", school: "Life", rank: 8, type: "Healing", pipCost: 8, description: "Heal all allies and grant them a protective ward." },
  { id: "banshee", name: "Banshee", school: "Death", rank: 3, type: "Damage", pipCost: 3, description: "Death damage to one enemy and weaken its next attack." },
  { id: "vampire", name: "Vampire", school: "Death", rank: 4, type: "Damage", pipCost: 4, description: "Damage one enemy and return some of that damage as health." },
  { id: "feint", name: "Feint", school: "Death", rank: 3, type: "Utility", pipCost: 1, description: "Place a large damage trap on one enemy, with a smaller drawback on the caster." },
  { id: "scarecrow", name: "Scarecrow", school: "Death", rank: 7, type: "Damage", pipCost: 7, description: "Damage all enemies and restore health to the caster based on damage dealt." },
  { id: "scarab", name: "Scarab", school: "Balance", rank: 1, type: "Damage", pipCost: 1, description: "Single-target Balance damage." },
  { id: "sandstorm", name: "Sandstorm", school: "Balance", rank: 4, type: "Damage", pipCost: 4, description: "Balance damage to all enemies." },
  { id: "power-nova", name: "Power Nova", school: "Balance", rank: 5, type: "Damage", pipCost: 5, description: "Balance damage to all enemies and weaken their next attacks." },
  { id: "judgement", name: "Judgement", school: "Balance", rank: 8, type: "Damage", pipCost: 0, variablePipCost: true, description: "Single-target Balance damage that scales with pips spent." },
  { id: "shadow-shrike", name: "Shadow Shrike", school: "Shadow", rank: 5, type: "Shadow", pipCost: 0, shadowPipCost: 1, description: "Shadow transformation that boosts offensive pressure and armor piercing, with a backlash drawback." },
  { id: "shadow-sentinel", name: "Shadow Sentinel", school: "Shadow", rank: 5, type: "Shadow", pipCost: 0, shadowPipCost: 1, description: "Shadow transformation focused on resistance and drawing enemy attacks, with a backlash drawback." },
  { id: "shadow-seraph", name: "Shadow Seraph", school: "Shadow", rank: 5, type: "Shadow", pipCost: 0, shadowPipCost: 1, description: "Shadow transformation focused on stronger healing, with a backlash drawback." }
];

let workspace = {
  activeDeckId: "default",
  decks: [{ id: "default", name: "Questing deck", encounter: "", notes: "", spellIds: [] }]
};
let deck = [];
let simulatedHand = [];
let remainingCards = [];
const selectedMulligans = new Set();
const schoolFilter = document.querySelector("#school-filter");
const typeFilter = document.querySelector("#type-filter");
const spellSearch = document.querySelector("#spell-search");
const catalog = document.querySelector("#spell-catalog");
const deckList = document.querySelector("#deck-list");
const emptyState = document.querySelector("#empty-state");
const noResults = document.querySelector("#no-results");
const cardCount = document.querySelector("#card-count");
const averagePips = document.querySelector("#average-pips");
const progress = document.querySelector(".progress-track");
const progressFill = document.querySelector("#progress-fill");
const resultCount = document.querySelector("#result-count");
const deckPicker = document.querySelector("#deck-picker");
const deckNameInput = document.querySelector("#deck-name");
const deckEncounterInput = document.querySelector("#deck-encounter");
const deckNotesInput = document.querySelector("#deck-notes");
const workspaceStatus = document.querySelector("#workspace-status");
const analysisSummary = document.querySelector("#analysis-summary");
const analysisBreakdown = document.querySelector("#analysis-breakdown");
const handList = document.querySelector("#hand-list");
const handEmpty = document.querySelector("#hand-empty");
const mulliganButton = document.querySelector("#mulligan-hand");

function createDeckRecord(name, spellIds = []) {
  return {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    name,
    encounter: "",
    notes: "",
    spellIds
  };
}

function restoreWorkspace() {
  let savedWorkspace;
  try {
    savedWorkspace = localStorage.getItem(SAVED_DECK_KEY);
    if (savedWorkspace === null) {
      const legacyDeck = localStorage.getItem(LEGACY_DECK_KEY);
      if (legacyDeck === null) return;
      const spellIds = JSON.parse(legacyDeck);
      if (!Array.isArray(spellIds)) {
        throw new Error("The saved deck data is not a list of spell IDs.");
      }
      workspace.decks[0].spellIds = spellIds.filter((id) => typeof id === "string");
      console.info("Migrated the saved single deck into a deck workspace.");
      return;
    }
  } catch (error) {
    console.error("Could not read the saved workspace from this browser.", error);
    return;
  }

  try {
    const parsed = JSON.parse(savedWorkspace);
    if (!parsed || !Array.isArray(parsed.decks) || parsed.decks.length === 0) {
      throw new Error("The saved workspace does not contain any decks.");
    }
    const restoredDecks = parsed.decks
      .filter((record) => record && typeof record.id === "string" && Array.isArray(record.spellIds))
      .map((record, index) => ({
        id: record.id,
        name: typeof record.name === "string" && record.name.trim() ? record.name.trim() : `Deck ${index + 1}`,
        encounter: typeof record.encounter === "string" ? record.encounter.slice(0, 64) : "",
        notes: typeof record.notes === "string" ? record.notes.slice(0, 500) : "",
        spellIds: record.spellIds.filter((id) => typeof id === "string")
      }));
    if (restoredDecks.length === 0) {
      throw new Error("The saved workspace does not contain any valid decks.");
    }
    workspace = {
      activeDeckId: restoredDecks.some((record) => record.id === parsed.activeDeckId)
        ? parsed.activeDeckId
        : restoredDecks[0].id,
      decks: restoredDecks
    };
  } catch (error) {
    console.error("The saved workspace data could not be restored.", error);
  }
}

function activeDeckRecord() {
  return workspace.decks.find((record) => record.id === workspace.activeDeckId);
}

function loadActiveDeck() {
  const record = activeDeckRecord();
  deck = [];
  simulatedHand = [];
  remainingCards = [];
  selectedMulligans.clear();
  if (!record) return;

  for (const id of record.spellIds) {
    const spell = spells.find((candidate) => candidate.id === id);
    if (!spell) {
      console.warn(`Skipping unknown saved spell ID: ${id}`);
      continue;
    }
    if (deck.length === MAX_DECK_SIZE) {
      console.warn("The saved deck exceeds the 64-card limit; extra cards were skipped.");
      break;
    }
    deck.push(spell);
  }
}

function saveDeck() {
  const record = activeDeckRecord();
  if (!record) return;
  record.spellIds = deck.map((spell) => spell.id);
  try {
    localStorage.setItem(SAVED_DECK_KEY, JSON.stringify(workspace));
  } catch (error) {
    console.error("Could not save the workspace in this browser.", error);
  }
}

function renderWorkspace() {
  const currentRecord = activeDeckRecord();
  deckPicker.replaceChildren();
  for (const record of workspace.decks) {
    const option = document.createElement("option");
    option.value = record.id;
    option.textContent = record.name;
    option.selected = record.id === workspace.activeDeckId;
    deckPicker.append(option);
  }
  if (document.activeElement !== deckNameInput) deckNameInput.value = currentRecord.name;
  if (document.activeElement !== deckEncounterInput) deckEncounterInput.value = currentRecord.encounter;
  if (document.activeElement !== deckNotesInput) deckNotesInput.value = currentRecord.notes;
  document.querySelector("#delete-deck").disabled = workspace.decks.length === 1;
}

function setWorkspaceStatus(message, isError = false) {
  workspaceStatus.textContent = message;
  workspaceStatus.classList.toggle("status-error", isError);
}

function resetSimulator() {
  simulatedHand = [];
  remainingCards = [];
  selectedMulligans.clear();
}

function renderHand() {
  handList.replaceChildren();
  handEmpty.hidden = simulatedHand.length > 0;
  const canDrawAgain = deck.length > 0;
  document.querySelector("#draw-hand").disabled = !canDrawAgain;

  simulatedHand.forEach((card, index) => {
    const button = document.createElement("button");
    button.className = `hand-card${selectedMulligans.has(card.instanceId) ? " is-mulligan" : ""}`;
    button.type = "button";
    button.setAttribute("aria-pressed", String(selectedMulligans.has(card.instanceId)));
    button.setAttribute("aria-label", `${card.spell.name}, ${selectedMulligans.has(card.instanceId) ? "marked to redraw" : "kept"}`);
    const name = document.createElement("strong");
    name.textContent = card.spell.name;
    const details = document.createElement("span");
    details.textContent = `${card.spell.school} · ${formatSpellCost(card.spell)}`;
    const action = document.createElement("span");
    action.className = "hand-action";
    action.textContent = selectedMulligans.has(card.instanceId) ? "Mulligan" : "Keep";
    button.append(name, details, action);
    button.addEventListener("click", () => {
      if (selectedMulligans.has(card.instanceId)) {
        selectedMulligans.delete(card.instanceId);
      } else {
        selectedMulligans.add(card.instanceId);
      }
      renderHand();
    });
    handList.append(button);
  });

  const selectedCount = selectedMulligans.size;
  mulliganButton.hidden = simulatedHand.length === 0;
  mulliganButton.disabled = selectedCount === 0 || remainingCards.length === 0;
  mulliganButton.textContent = selectedCount > 0
    ? `Redraw ${selectedCount} selected ${selectedCount === 1 ? "card" : "cards"}`
    : "Select cards to mulligan";
}

function renderAnalysis() {
  const typeCounts = new Map();
  const pipCounts = new Map();
  const schoolCounts = new Map();
  for (const spell of deck) {
    typeCounts.set(spell.type, (typeCounts.get(spell.type) || 0) + 1);
    schoolCounts.set(spell.school, (schoolCounts.get(spell.school) || 0) + 1);
    if (!spell.variablePipCost) {
      const cost = spell.pipCost;
      pipCounts.set(cost, (pipCounts.get(cost) || 0) + 1);
    }
  }

  const uniqueCards = new Set(deck.map((spell) => spell.id)).size;
  analysisSummary.textContent = `${uniqueCards} unique · ${deck.length - uniqueCards} duplicate${deck.length - uniqueCards === 1 ? "" : "s"}`;
  analysisBreakdown.replaceChildren();

  const groups = [
    { title: "Spell types", counts: typeCounts, order: ["Damage", "Healing", "Defense", "Utility", "Shadow"] },
    { title: "Pip curve", counts: pipCounts, order: [...pipCounts.keys()].sort((a, b) => a - b).map(String), numeric: true },
    { title: "Schools", counts: schoolCounts, order: [...schoolCounts.keys()].sort() }
  ];
  for (const group of groups) {
    const section = document.createElement("div");
    section.className = "analysis-group";
    const heading = document.createElement("h4");
    heading.textContent = group.title;
    section.append(heading);
    const entries = group.order
      .map((key) => [key, group.counts.get(group.numeric ? Number(key) : key) || 0])
      .filter(([, count]) => count > 0);
    if (entries.length === 0) {
      const empty = document.createElement("p");
      empty.className = "analysis-empty";
      empty.textContent = "Add cards to see this breakdown.";
      section.append(empty);
    } else {
      const maxCount = Math.max(...entries.map(([, count]) => count));
      for (const [label, count] of entries) {
        const row = document.createElement("div");
        row.className = "analysis-row";
        const caption = document.createElement("span");
        caption.textContent = group.numeric ? `${label} pip${label === "1" ? "" : "s"}` : label;
        const bar = document.createElement("span");
        bar.className = "analysis-bar";
        bar.setAttribute("aria-hidden", "true");
        const fill = document.createElement("span");
        fill.style.width = `${(count / maxCount) * 100}%`;
        bar.append(fill);
        const value = document.createElement("strong");
        value.textContent = String(count);
        row.append(caption, bar, value);
        section.append(row);
      }
    }
    analysisBreakdown.append(section);
  }
}

function formatSpellCost(spell) {
  const costs = [];
  if (spell.variablePipCost) {
    costs.push("Variable pip cost");
  }
  if (spell.pipCost > 0) {
    costs.push(`${spell.pipCost} ${spell.pipCost === 1 ? "pip" : "pips"}`);
  }
  if (spell.shadowPipCost) {
    costs.push(`${spell.shadowPipCost} ${spell.shadowPipCost === 1 ? "Shadow pip" : "Shadow pips"}`);
  }
  return costs.length > 0 ? costs.join(" + ") : "0 pips";
}

function renderCatalog() {
  const selectedSchool = schoolFilter.value;
  const selectedType = typeFilter.value;
  const searchTerm = spellSearch.value.trim().toLowerCase();
  const availableSpells = spells.filter((spell) =>
    (selectedSchool === "All Schools" || spell.school === selectedSchool) &&
    (selectedType === "All Types" || spell.type === selectedType) &&
    `${spell.name} ${spell.school} ${spell.type} rank ${spell.rank} ${spell.description}`.toLowerCase().includes(searchTerm)
  );

  catalog.replaceChildren();
  noResults.hidden = availableSpells.length > 0;
  resultCount.textContent = `${availableSpells.length} ${availableSpells.length === 1 ? "spell" : "spells"}`;

  for (const spell of availableSpells) {
    const card = document.createElement("article");
    card.className = "spell-card";

    const school = document.createElement("span");
    school.className = `school-tag school-${spell.school.toLowerCase()}`;
    school.textContent = spell.school;

    const name = document.createElement("h3");
    name.textContent = spell.name;

    const metadata = document.createElement("p");
    metadata.className = "spell-meta";
    metadata.textContent = `Rank ${spell.rank} · ${spell.type}`;

    const description = document.createElement("p");
    description.className = "spell-description";
    description.textContent = spell.description;

    const footer = document.createElement("div");
    footer.className = "spell-footer";
    const cost = document.createElement("span");
    cost.className = "pip-cost";
    cost.textContent = formatSpellCost(spell);

    const addButton = document.createElement("button");
    addButton.className = "button button-primary";
    addButton.type = "button";
    addButton.textContent = "Add to deck";
    addButton.disabled = deck.length >= MAX_DECK_SIZE;
    addButton.addEventListener("click", () => {
      if (deck.length < MAX_DECK_SIZE) {
        deck.push(spell);
        resetSimulator();
        render();
      }
    });

    footer.append(cost, addButton);
    card.append(school, name, metadata, description, footer);
    catalog.append(card);
  }
}

function renderDeck() {
  deckList.replaceChildren();
  emptyState.hidden = deck.length > 0;

  deck.forEach((spell, index) => {
    const item = document.createElement("article");
    item.className = "deck-card";

    const details = document.createElement("div");
    const name = document.createElement("h3");
    name.textContent = spell.name;
    const meta = document.createElement("p");
    meta.textContent = `${spell.school} · Rank ${spell.rank} · ${formatSpellCost(spell)}`;
    details.append(name, meta);

    const removeButton = document.createElement("button");
    removeButton.className = "remove-button";
    removeButton.type = "button";
    removeButton.textContent = "Remove";
    removeButton.setAttribute("aria-label", `Remove ${spell.name} from deck`);
    removeButton.addEventListener("click", () => {
      deck.splice(index, 1);
      resetSimulator();
      render();
    });

    item.append(details, removeButton);
    deckList.append(item);
  });
}

function renderStats() {
  const fixedCostSpells = deck.filter((spell) => !spell.variablePipCost);
  const pipTotal = fixedCostSpells.reduce((total, spell) => total + spell.pipCost, 0);
  const count = deck.length;
  cardCount.replaceChildren(document.createTextNode(String(count)));
  const capacity = document.createElement("span");
  capacity.textContent = ` / ${MAX_DECK_SIZE} cards`;
  cardCount.append(capacity);
  averagePips.textContent = (fixedCostSpells.length === 0 ? 0 : pipTotal / fixedCostSpells.length).toFixed(1);
  progress.setAttribute("aria-valuenow", String(count));
  progressFill.style.width = `${(count / MAX_DECK_SIZE) * 100}%`;
}

function render() {
  renderWorkspace();
  renderCatalog();
  renderDeck();
  renderStats();
  renderAnalysis();
  renderHand();
  saveDeck();
}

schoolFilter.addEventListener("change", renderCatalog);
typeFilter.addEventListener("change", renderCatalog);
spellSearch.addEventListener("input", renderCatalog);
deckPicker.addEventListener("change", () => {
  workspace.activeDeckId = deckPicker.value;
  loadActiveDeck();
  setWorkspaceStatus(`Switched to ${activeDeckRecord().name}.`);
  render();
});
document.querySelector("#new-deck").addEventListener("click", () => {
  const record = createDeckRecord(`New deck ${workspace.decks.length + 1}`);
  workspace.decks.push(record);
  workspace.activeDeckId = record.id;
  loadActiveDeck();
  setWorkspaceStatus("New deck created.");
  render();
  deckNameInput.focus();
  deckNameInput.select();
});
document.querySelector("#duplicate-deck").addEventListener("click", () => {
  const current = activeDeckRecord();
  const duplicate = createDeckRecord(`${current.name} copy`, [...current.spellIds]);
  duplicate.encounter = current.encounter;
  duplicate.notes = current.notes;
  workspace.decks.push(duplicate);
  workspace.activeDeckId = duplicate.id;
  loadActiveDeck();
  setWorkspaceStatus(`Created a copy of ${current.name}.`);
  render();
});
document.querySelector("#delete-deck").addEventListener("click", () => {
  const current = activeDeckRecord();
  if (workspace.decks.length === 1 || !window.confirm(`Delete "${current.name}" and its saved cards?`)) return;
  workspace.decks = workspace.decks.filter((record) => record.id !== current.id);
  workspace.activeDeckId = workspace.decks[0].id;
  loadActiveDeck();
  setWorkspaceStatus(`Deleted ${current.name}.`);
  render();
});
for (const [input, property] of [
  [deckNameInput, "name"],
  [deckEncounterInput, "encounter"],
  [deckNotesInput, "notes"]
]) {
  input.addEventListener("input", () => {
    const record = activeDeckRecord();
    record[property] = input.value.trim() || (property === "name" ? "Untitled deck" : "");
    renderWorkspace();
    saveDeck();
  });
  input.addEventListener("blur", () => {
    if (property === "name" && !input.value.trim()) {
      input.value = activeDeckRecord().name;
    }
  });
}
const requestedSchool = new URLSearchParams(window.location.search).get("school");
if ([...schoolFilter.options].some((option) => option.value === requestedSchool)) {
  schoolFilter.value = requestedSchool;
}
document.querySelector("#reset-deck").addEventListener("click", () => {
  if (deck.length > 0 && !window.confirm(`Remove all cards from "${activeDeckRecord().name}"?`)) return;
  deck.length = 0;
  resetSimulator();
  setWorkspaceStatus("The active deck has been cleared.");
  render();
});
document.querySelector("#export-deck").addEventListener("click", () => {
  const cards = deck.map((spell) =>
    `- ${spell.name} (${spell.school}, Rank ${spell.rank}, ${formatSpellCost(spell)})`
  );
  const fixedCostSpells = deck.filter((spell) => !spell.variablePipCost);
  const totalPips = fixedCostSpells.reduce((total, spell) => total + spell.pipCost, 0);
  const summary = [
    "Wizard101 Deck Builder — Deck Export",
    `Deck: ${activeDeckRecord().name}`,
    ...(activeDeckRecord().encounter ? [`Encounter: ${activeDeckRecord().encounter}`] : []),
    `Cards: ${deck.length}/${MAX_DECK_SIZE}`,
    `Average fixed pip cost: ${fixedCostSpells.length === 0 ? "0.0" : (totalPips / fixedCostSpells.length).toFixed(1)} (variable-cost spells excluded)`,
    "",
    ...(cards.length > 0 ? cards : ["(No cards in this deck yet.)"]),
    ...(activeDeckRecord().notes ? ["", "Strategy notes:", activeDeckRecord().notes] : []),
    "",
    "Unofficial fan-made planner. Not affiliated with KingsIsle Entertainment."
  ].join("\n");
  const download = document.createElement("a");
  download.href = URL.createObjectURL(new Blob([summary], { type: "text/plain;charset=utf-8" }));
  download.download = "wizard101-deck.txt";
  download.click();
  window.setTimeout(() => URL.revokeObjectURL(download.href), 1000);
});

function downloadFile(filename, content, type) {
  const objectUrl = URL.createObjectURL(new Blob([content], { type }));
  const download = document.createElement("a");
  download.href = objectUrl;
  download.download = filename;
  download.click();
  window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}

document.querySelector("#export-backup").addEventListener("click", () => {
  saveDeck();
  const backup = {
    format: "wizard101-deck-builder-workspace",
    version: 1,
    exportedAt: new Date().toISOString(),
    activeDeckId: workspace.activeDeckId,
    decks: workspace.decks
  };
  downloadFile(
    "wizard101-deck-workspace.json",
    JSON.stringify(backup, null, 2),
    "application/json;charset=utf-8"
  );
  setWorkspaceStatus(`Exported ${workspace.decks.length} deck${workspace.decks.length === 1 ? "" : "s"} to a JSON backup.`);
});

const backupFile = document.querySelector("#backup-file");
document.querySelector("#import-backup").addEventListener("click", () => backupFile.click());
backupFile.addEventListener("change", async () => {
  const file = backupFile.files[0];
  if (!file) return;
  try {
    const parsed = JSON.parse(await file.text());
    if (
      parsed?.format !== "wizard101-deck-builder-workspace" ||
      parsed.version !== 1 ||
      !Array.isArray(parsed.decks) ||
      parsed.decks.length === 0
    ) {
      throw new Error("Choose a valid Wizard101 Deck Builder workspace backup.");
    }
    const importedDecks = parsed.decks.map((record, index) => {
      if (!record || typeof record !== "object" || !Array.isArray(record.spellIds)) {
        throw new Error(`Deck ${index + 1} in the backup is not valid.`);
      }
      return {
        ...createDeckRecord(
          typeof record.name === "string" && record.name.trim() ? record.name.trim().slice(0, 48) : `Deck ${index + 1}`,
          record.spellIds.filter((id) => typeof id === "string")
        ),
        encounter: typeof record.encounter === "string" ? record.encounter.slice(0, 64) : "",
        notes: typeof record.notes === "string" ? record.notes.slice(0, 500) : "",
        previousId: typeof record.id === "string" ? record.id : ""
      };
    });
    if (!window.confirm(`Replace your ${workspace.decks.length} saved deck${workspace.decks.length === 1 ? "" : "s"} with ${importedDecks.length} deck${importedDecks.length === 1 ? "" : "s"} from this backup?`)) {
      return;
    }
    const importedActive = importedDecks.find((record) => record.previousId === parsed.activeDeckId);
    workspace = {
      activeDeckId: importedActive ? importedActive.id : importedDecks[0].id,
      decks: importedDecks.map(({ previousId, ...record }) => record)
    };
    loadActiveDeck();
    render();
    setWorkspaceStatus(`Imported ${workspace.decks.length} deck${workspace.decks.length === 1 ? "" : "s"} from backup.`);
  } catch (error) {
    console.error("Could not import the workspace backup.", error);
    setWorkspaceStatus(error instanceof SyntaxError ? "The selected file is not valid JSON." : error.message, true);
  } finally {
    backupFile.value = "";
  }
});

document.querySelector("#draw-hand").addEventListener("click", () => {
  const shuffled = deck.map((spell, instanceId) => ({ spell, instanceId }));
  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [shuffled[index], shuffled[swapIndex]] = [shuffled[swapIndex], shuffled[index]];
  }
  simulatedHand = shuffled.slice(0, 7);
  remainingCards = shuffled.slice(simulatedHand.length);
  selectedMulligans.clear();
  renderHand();
});

mulliganButton.addEventListener("click", () => {
  if (selectedMulligans.size === 0 || remainingCards.length === 0) return;
  const keepers = simulatedHand.filter((card) => !selectedMulligans.has(card.instanceId));
  const redrawCount = Math.min(selectedMulligans.size, remainingCards.length);
  const returnedCards = simulatedHand.filter((card) => selectedMulligans.has(card.instanceId));
  const replacements = remainingCards.splice(0, redrawCount);
  remainingCards.push(...returnedCards);
  simulatedHand = [...keepers, ...replacements];
  selectedMulligans.clear();
  renderHand();
});

restoreWorkspace();
loadActiveDeck();
render();

if ("serviceWorker" in navigator && window.location.protocol !== "file:") {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("service-worker.js")
      .catch((error) => console.error("Could not register the offline app cache.", error));
  });
}
