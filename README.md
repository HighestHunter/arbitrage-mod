# HugoSMP Arbitrage (Fabric, Minecraft 1.21.4)

Zeigt per Hotkey (Standard: J) die Arbitrage-Liste live an, aktualisiert alle `refreshSeconds` Sekunden.
Taste umbinden: Optionen -> Steuerung -> "HugoSMP Arbitrage" -> "Arbitrage öffnen".
Gleiche Taste im Fenster schliesst es wieder. Mausrad scrollt.

## WICHTIG: API-URL eintragen
Die Webseite laedt ihre Daten per JavaScript. Die echte Endpoint-URL findest du so:
1. https://hugosmp-market.net/en/arbitrage im Browser oeffnen
2. F12 -> Reiter "Network" -> Filter "Fetch/XHR" -> Seite neu laden
3. Den Request mit den Arbitrage-Daten anklicken -> URL kopieren
4. In `config/hugoarb.json` bei `apiUrl` eintragen (Mod einmal starten, dann wird die Datei erzeugt)
   Braucht der Request einen Header/Key (z.B. Market+), `authHeaderName` / `authHeaderValue` setzen.

Der Parser sucht automatisch ein JSON-Array und erkennt gaengige Feldnamen (item/name, buy/ah, sell/order, profit ...).
Passen die Namen nicht, einfach in `ArbitrageParser.java` die Listen oben erweitern.

## Bauen
    gradle wrapper --gradle-version 8.12   (einmalig)
    ./gradlew build
Die Mod liegt danach in build/libs/hugosmp-arbitrage-1.0.0.jar
(Alternativ den Ordner in das offizielle Fabric-Example-Mod-Template kopieren.)

## Modrinth
Projekt auf modrinth.com anlegen, Typ "Mod", Loader Fabric, Version 1.21.4, Client-side, die jar hochladen.
