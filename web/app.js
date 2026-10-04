const MAX_DECK_SIZE = 64;
const SAVED_DECK_KEY = "wizard101-deck-builder";

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

const deck = [];
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

function restoreDeck() {
  let savedDeck;
  try {
    savedDeck = localStorage.getItem(SAVED_DECK_KEY);
  } catch (error) {
    console.error("Could not read the saved deck from this browser.", error);
    return;
  }

  if (!savedDeck) {
    return;
  }

  let spellIds;
  try {
    spellIds = JSON.parse(savedDeck);
  } catch (error) {
    console.error("The saved deck data is not valid JSON.", error);
    return;
  }

  if (!Array.isArray(spellIds)) {
    console.error("The saved deck data is not a list of spell IDs.");
    return;
  }

  for (const id of spellIds) {
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
  try {
    localStorage.setItem(SAVED_DECK_KEY, JSON.stringify(deck.map((spell) => spell.id)));
  } catch (error) {
    console.error("Could not save the deck in this browser.", error);
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
  renderCatalog();
  renderDeck();
  renderStats();
  saveDeck();
}

schoolFilter.addEventListener("change", renderCatalog);
typeFilter.addEventListener("change", renderCatalog);
spellSearch.addEventListener("input", renderCatalog);
const requestedSchool = new URLSearchParams(window.location.search).get("school");
if ([...schoolFilter.options].some((option) => option.value === requestedSchool)) {
  schoolFilter.value = requestedSchool;
}
document.querySelector("#reset-deck").addEventListener("click", () => {
  deck.length = 0;
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
    `Cards: ${deck.length}/${MAX_DECK_SIZE}`,
    `Average fixed pip cost: ${fixedCostSpells.length === 0 ? "0.0" : (totalPips / fixedCostSpells.length).toFixed(1)} (variable-cost spells excluded)`,
    "",
    ...(cards.length > 0 ? cards : ["(No cards in this deck yet.)"]),
    "",
    "Unofficial fan-made planner. Not affiliated with KingsIsle Entertainment."
  ].join("\n");
  const download = document.createElement("a");
  download.href = URL.createObjectURL(new Blob([summary], { type: "text/plain;charset=utf-8" }));
  download.download = "wizard101-deck.txt";
  download.click();
  window.setTimeout(() => URL.revokeObjectURL(download.href), 1000);
});

restoreDeck();
render();

if ("serviceWorker" in navigator && window.location.protocol !== "file:") {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("service-worker.js")
      .catch((error) => console.error("Could not register the offline app cache.", error));
  });
}
