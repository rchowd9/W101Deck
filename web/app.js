const MAX_DECK_SIZE = 64;

const spells = [
  { id: "fire-cat", name: "Fire Cat", school: "Fire", pipCost: 1, description: "80–120 Fire Damage" },
  { id: "heckhound", name: "Heckhound", school: "Fire", pipCost: 1, description: "130 Fire Damage per Pip over 3 turns" },
  { id: "storm-shark", name: "Storm Shark", school: "Storm", pipCost: 3, description: "375–435 Storm Damage" },
  { id: "tower-shield", name: "Tower Shield", school: "Ice", pipCost: 0, description: "−50% to next damage spell" },
  { id: "satyr", name: "Satyr", school: "Life", pipCost: 4, description: "860 Health to target" }
];

const deck = [];
const schoolFilter = document.querySelector("#school-filter");
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

function renderCatalog() {
  const selectedSchool = schoolFilter.value;
  const searchTerm = spellSearch.value.trim().toLowerCase();
  const availableSpells = spells.filter((spell) =>
    (selectedSchool === "All Schools" || spell.school === selectedSchool) &&
    `${spell.name} ${spell.school} ${spell.description}`.toLowerCase().includes(searchTerm)
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

    const description = document.createElement("p");
    description.className = "spell-description";
    description.textContent = spell.description;

    const footer = document.createElement("div");
    footer.className = "spell-footer";
    const cost = document.createElement("span");
    cost.className = "pip-cost";
    cost.textContent = `${spell.pipCost} ${spell.pipCost === 1 ? "pip" : "pips"}`;

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
    card.append(school, name, description, footer);
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
    meta.textContent = `${spell.school} · ${spell.pipCost} ${spell.pipCost === 1 ? "pip" : "pips"}`;
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
  const pipTotal = deck.reduce((total, spell) => total + spell.pipCost, 0);
  const count = deck.length;
  cardCount.replaceChildren(document.createTextNode(String(count)));
  const capacity = document.createElement("span");
  capacity.textContent = ` / ${MAX_DECK_SIZE} cards`;
  cardCount.append(capacity);
  averagePips.textContent = (count === 0 ? 0 : pipTotal / count).toFixed(1);
  progress.setAttribute("aria-valuenow", String(count));
  progressFill.style.width = `${(count / MAX_DECK_SIZE) * 100}%`;
}

function render() {
  renderCatalog();
  renderDeck();
  renderStats();
}

schoolFilter.addEventListener("change", renderCatalog);
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
    `- ${spell.name} (${spell.school}, ${spell.pipCost} ${spell.pipCost === 1 ? "pip" : "pips"})`
  );
  const totalPips = deck.reduce((total, spell) => total + spell.pipCost, 0);
  const summary = [
    "Wizard101 Deck Builder — Deck Export",
    `Cards: ${deck.length}/${MAX_DECK_SIZE}`,
    `Average pip cost: ${deck.length === 0 ? "0.0" : (totalPips / deck.length).toFixed(1)}`,
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

render();

if ("serviceWorker" in navigator && window.location.protocol !== "file:") {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("service-worker.js")
      .catch((error) => console.error("Could not register the offline app cache.", error));
  });
}
