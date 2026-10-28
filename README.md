# Skyblock Bingo – Fabric-Mod für Minecraft 26.2

Bingo mit HUD: eigene Goals, Board 3×3 bis 7×7, Lockout an/aus, Diagonalen zählen, Timer (zählt hoch),
Sieg-Meldung sobald jemand eine Reihe hat, Multiplayer (1–2 Spieler) per Party-Code, hell/dunkel/eigene Farben.

Der Mod ist rein clientseitig und liest nichts aus dem Spiel aus: Felder werden nur von dir per Befehl abgehakt.

## .jar bauen (ohne Installation, über GitHub)

1. Kostenloses Konto auf github.com anlegen und ein neues Repository erstellen (z. B. `skyblock-bingo`).
2. Den kompletten Inhalt dieses Ordners hochladen (Add file → Upload files). Wichtig: auch der Ordner
   `.github/workflows` mit `build.yml` muss dabei sein. Falls er fehlt: Add file → Create new file,
   als Name `.github/workflows/build.yml` eintippen und den Inhalt der Datei einfügen.
3. Oben auf den Reiter **Actions** gehen. Der Bau startet automatisch (dauert etwa 3–5 Minuten).
4. Wenn er grün ist: Bau anklicken, unten bei **Artifacts** `skyblock-bingo-jar` herunterladen.
5. In der ZIP liegt `skyblock-bingo-1.0.0.jar`. In den `mods`-Ordner legen, zusammen mit dem **Fabric API** Mod.

Schlägt der Bau fehl, die rot markierte Fehlermeldung aus dem Actions-Log kopieren und mir schicken.

## .jar selbst bauen

Java 25 und Gradle 9.5.1 installieren, im Projektordner `gradle build` ausführen.
Die Datei liegt danach in `build/libs/`.

## Befehle (alle unter /sbb, weil /bingo schon Hypixel gehört)

| Befehl | Wirkung |
|---|---|
| `/sbb goal add <text>` | Goal hinzufügen (mehrere mit `;` trennen) |
| `/sbb goal list [seite]` / `remove <nr>` / `clear` / `example` | Goals verwalten, `example` lädt 30 Beispiele |
| `/sbb list save <name>` / `load <name>` / `delete <name>` / `show` | Goal-Listen dauerhaft speichern |
| `/sbb size <3-7>` | Board-Größe |
| `/sbb lockout <true/false>` | Lockout an oder aus |
| `/sbb solo` | Alleine spielen |
| `/sbb host` | Party erstellen, zeigt den Party-Code |
| `/sbb join <code>` | Party beitreten |
| `/sbb start` | Runde starten (Host oder Solo) |
| `/sbb mark <nr>` | Feld abhaken oder wieder entfernen (Nummer steht oben links im Feld) |
| `/sbb end` / `leave` | Runde beenden / Party verlassen |
| `/sbb theme dark/light/custom` | Design |
| `/sbb color <bg/cell/text/muted/p0/p1/border> <RRGGBB>` | eigene Farben (schaltet auf custom) |
| `/sbb hud show/hide`, `hud pos <x> <y>`, `hud cell <px>` | HUD anzeigen, verschieben, Feldgröße |
| `/sbb name <name>` | Anzeigename |

Goals und Einstellungen werden automatisch gespeichert in `config/skyblockbingo.json`.

## Multiplayer

Der Host tippt `/sbb host` und gibt den 6-stelligen Code weiter, der Mitspieler tippt `/sbb join <code>`.
Die Nachrichten laufen über den öffentlichen Dienst ntfy.sh (kein eigener Server nötig, Internet erforderlich).
Der Code ist der Kanalname und lässt sich nur raten, nicht verstecken: Gib ihn nur Leuten, die mitspielen sollen.
Goal-Texte sind auf 50 Zeichen begrenzt, damit die Nachrichten klein bleiben.
