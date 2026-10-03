# Wizard101 Deck Builder

The JavaFX desktop app includes a spell deck builder with local deck
saving/export, a pet talent pool calculator, a per-character crafting and
reagent tracker, and a world progression checklist. Planner records are stored
in a local SQLite database at `~/.wizard101-deck-builder/planner.db`; no game
account is needed.

The pet calculator uses an explicitly illustrative independent-chance model
based on the percentages entered for each parent; it does not claim to reproduce
Wizard101's in-game hatching mechanics. Crafting recipes and progression items
are user-entered so requirements can be matched to the player's current game
version and character.

This repository contains a JavaFX desktop app and a separate static web edition.
Both let you filter the sample spell catalog, add and remove cards, track deck
size up to 64 cards, and reset the deck. The web edition also provides spell
search, browser-local deck saving, text export, an attributed Myth-school field
guide, and an offline-ready installable shell.

## Run the JavaFX desktop app

Install JDK 17 or newer and Maven, then run this from the repository root:

```powershell
mvn clean javafx:run
```

On Windows, you can instead double-click `run-desktop.cmd` or run:

```powershell
.\run-desktop.cmd
```

Maven downloads the JavaFX dependencies on the first run. A desktop environment
is required; the JavaFX app is not a server application.

## Run the web edition locally

Serve the `web` folder with any static file server to use all browser features
and enable the offline app cache. For example, with Python installed:

```powershell
py -m http.server 8000 --directory web
```

Then open `http://localhost:8000`. The browser edition has no build step or
package dependencies. Directly opening `index.html` also works, but browsers
only enable the offline app cache on localhost or HTTPS.

## Deploy the web edition to Vercel

Vercel hosts the browser edition; it cannot run the JavaFX desktop GUI.

1. Push the project to GitHub and import that repository in Vercel.
2. Keep the project **Root Directory** at the repository root.
3. Choose the **Other** framework preset and leave the build command empty.
   The root `vercel.json` sets `web` as the static output directory.
4. Deploy. Vercel will publish the browser edition from `web`; it does not run
   the JavaFX desktop GUI.

Alternatively, install Node.js, then install and run the Vercel CLI from the
repository root:

```powershell
npm install --global vercel
vercel
vercel --prod
```

The first `vercel` command links the project and creates a preview deployment;
`vercel --prod` publishes to production. The account must be logged in with
`vercel login`.

## Wizard101 reference

The [school field guide](web/schools.html) paraphrases the Myth-school
description available from the [official Wizard101 website](https://www.wizard101.com/).
The guide labels its deck-planning suggestions as fan-made advice; verify current
spell details in-game. See [web/SOURCES.md](web/SOURCES.md) for attribution and
catalog limitations.
