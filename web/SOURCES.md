# Reference notes

## Official Wizard101 school description

- **Publisher:** KingsIsle Entertainment, official Wizard101 website
- **URL:** <https://www.wizard101.com/>
- **Accessed:** October 2, 2026
- **Used for:** The Myth school overview in `schools.html`

The official site currently routes differently depending on region and page
availability. The accessed page described Myth magic as imagination made real,
identified Myth wizards as Conjurers, and described minion summoning, minion
support spells, and removing enemy defenses. The guide paraphrases those
details and labels its deck-building suggestions as fan-made advice.

The Shadow and other school profiles in `schools.html` are fan-made deck-planning
suggestions, not official school descriptions. Their links open the corresponding
school filter in the sample catalog.

The spell list in `app.js` is a hand-curated fan-made sample catalog, not a
complete or authoritative spell database. Ranks are included as a planning aid;
the Shadow entries describe broad transformation roles rather than exact
mechanics, and variable pip costs are identified separately. Check current
in-game details before relying on spell values.

## Optional Wizardeo spell catalog import

- **Publisher:** Wizardeo, community Wizard101 fansite
- **URL:** <https://tools.wizardeo.eu/deck-builder/>
- **Catalog endpoint:** Wizardeo's public, read-only `tools_deck_builder`
  catalog, requested by the site itself for its deck builder
- **Accessed:** November 25, 2026
- **Used for:** The optional browser-side **Import Wizardeo catalog** action in
  `app.js`

The importer maps the English name, school, unlock level, spell type, pip
costs, accuracy, effect metadata, description, and Wizard101 Central reference
URL into this planner's local JSON records. Unlock level is shown as a level,
not mislabeled as spell rank. X-pip or per-pip damage is omitted from numeric
damage estimates. Imported records are stored in browser local storage and
included in workspace JSON backups; no account credentials are collected.
Wizardeo and its upstream catalog may change or become unavailable. Spell
details and simplified combat estimates should be verified in-game.
