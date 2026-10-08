const CACHE_NAME = "wizard101-deck-builder-v5";
const APP_FILES = [
  "./",
  "./index.html",
  "./schools.html",
  "./styles.css",
  "./schools.css",
  "./app.js",
  "./favicon.svg",
  "./manifest.webmanifest"
];

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_FILES)));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key.startsWith("wizard101-deck-builder-") && key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET" || new URL(event.request.url).origin !== self.location.origin) {
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cachedResponse) =>
      cachedResponse || fetch(event.request).then((response) => {
        if (response.ok) {
          const responseCopy = response.clone();
          return caches.open(CACHE_NAME)
            .then((cache) => cache.put(event.request, responseCopy))
            .then(() => response);
        }
        return response;
      })
    )
  );
});
