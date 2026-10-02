# Wizard101 Deck Builder

This repository contains a JavaFX desktop app and a separate static web edition.
Both let you filter the sample spell catalog, add and remove cards, track deck
size up to 64 cards, and reset the deck. The web edition also provides spell
search, text export, an attributed Myth-school field guide, and an offline-ready
installable shell.

## Run the JavaFX desktop app

Install JDK 17 or newer and Maven, then run this from the repository root:

```powershell
mvn clean javafx:run
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

1. Import this repository in Vercel.
2. Set the project's **Root Directory** to `web`.
3. Leave the framework preset as **Other**, the build command empty, and set
   the output directory to `.`.
4. Deploy. The `web` folder contains the static site entry point and assets.

Alternatively, install the Vercel CLI and run `vercel --prod` from the `web`
directory.

## Wizard101 reference

The [school field guide](web/schools.html) paraphrases the Myth-school
description available from the [official Wizard101 website](https://www.wizard101.com/).
The guide labels its deck-planning suggestions as fan-made advice; verify current
spell details in-game. See [web/SOURCES.md](web/SOURCES.md) for attribution and
catalog limitations.
