# Wizard101 Deck Builder

This repository contains a JavaFX desktop app and a separate static web edition.
Both let you filter the sample spell catalog, add and remove cards, track deck
size up to 64 cards, and reset the deck.

## Run the JavaFX desktop app

Install JDK 17 or newer and Maven, then run this from the repository root:

```powershell
mvn clean javafx:run
```

Maven downloads the JavaFX dependencies on the first run. A desktop environment
is required; the JavaFX app is not a server application.

## Run the web edition locally

Open `web/index.html` in a browser, or serve the `web` folder with any static
file server. The browser edition has no build step or package dependencies.

## Deploy the web edition to Vercel

Vercel hosts the browser edition; it cannot run the JavaFX desktop GUI.

1. Import this repository in Vercel.
2. Set the project's **Root Directory** to `web`.
3. Leave the framework preset as **Other**, the build command empty, and set
   the output directory to `.`.
4. Deploy. The `web` folder contains the static site entry point and assets.

Alternatively, install the Vercel CLI and run `vercel --prod` from the `web`
directory.
