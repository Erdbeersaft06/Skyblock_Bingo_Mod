package dev.skyblockbingo;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Uebersetzungen: Deutsch (de), Englisch (en), Franzoesisch (fr), Spanisch (es). */
public final class Lang {
	public static final List<String> CODES = List.of("de", "en", "fr", "es");
	public static final List<String> NAMES = List.of("Deutsch", "English", "Français", "Español");

	private static final Map<String, String[]> T = new HashMap<>();

	private Lang() {}

	private static void add(String key, String de, String en, String fr, String es) {
		T.put(key, new String[] {de, en, fr, es});
	}

	public static int index() {
		int i = CODES.indexOf(Bingo.cfg.lang);
		return i < 0 ? 0 : i;
	}

	public static String languageName() {
		return NAMES.get(index());
	}

	public static String t(String key, Object... args) {
		String[] v = T.get(key);
		if (v == null) return key;
		String s = v[index()];
		return args.length == 0 ? s : String.format(Locale.ROOT, s, args);
	}

	/** Beispiel-Goals in der gewaehlten Sprache. */
	public static List<String> examples() {
		return EXAMPLES.get(index());
	}

	private static final List<List<String>> EXAMPLES = List.of(
		List.of(
			"Besiege 25 Zombies", "Besiege 15 Spinnen", "Besiege 10 Endermen", "Besiege einen Boss", "Besiege 20 Skelette",
			"Fische 20 Items", "Fische 5 Seemonster", "Baue 64 Kohle ab", "Baue 32 Eisenerz ab", "Baue 16 Diamanten ab",
			"Ernte 100 Weizen", "Ernte 64 Karotten", "Ernte 50 Kürbisse", "Baue 64 Holz ab", "Fälle 50 Bäume",
			"Verkaufe 10 Items im Bazaar", "Kaufe ein Item im Bazaar", "Crafte ein neues Werkzeug", "Verzaubere ein Item",
			"Schmiede ein Item", "Besuche die Insel eines Spielers", "Platziere 5 Minions", "Sammle 100 Minion-Items",
			"Erledige 3 Quests", "Löse einen Rätsel-Raum", "Töte 5 Mobs ohne Schaden", "Sammle 10 verschiedene Items",
			"Lege 500 Blöcke zu Fuß zurück", "Rette 3 Tiere", "Öffne 2 Truhen"),
		List.of(
			"Defeat 25 zombies", "Defeat 15 spiders", "Defeat 10 endermen", "Defeat a boss", "Defeat 20 skeletons",
			"Fish up 20 items", "Fish up 5 sea creatures", "Mine 64 coal", "Mine 32 iron ore", "Mine 16 diamonds",
			"Harvest 100 wheat", "Harvest 64 carrots", "Harvest 50 pumpkins", "Chop 64 wood", "Fell 50 trees",
			"Sell 10 items on the Bazaar", "Buy an item on the Bazaar", "Craft a new tool", "Enchant an item",
			"Forge an item", "Visit another player's island", "Place 5 minions", "Collect 100 minion items",
			"Complete 3 quests", "Solve a puzzle room", "Kill 5 mobs without taking damage", "Collect 10 different items",
			"Walk 500 blocks on foot", "Rescue 3 animals", "Open 2 chests"),
		List.of(
			"Bats 25 zombies", "Bats 15 araignées", "Bats 10 endermen", "Bats un boss", "Bats 20 squelettes",
			"Pêche 20 objets", "Pêche 5 créatures marines", "Mine 64 charbons", "Mine 32 minerais de fer", "Mine 16 diamants",
			"Récolte 100 blés", "Récolte 64 carottes", "Récolte 50 citrouilles", "Coupe 64 bois", "Abats 50 arbres",
			"Vends 10 objets au Bazaar", "Achète un objet au Bazaar", "Fabrique un nouvel outil", "Enchante un objet",
			"Forge un objet", "Visite l'île d'un joueur", "Place 5 minions", "Collecte 100 objets de minions",
			"Termine 3 quêtes", "Résous une salle d'énigme", "Tue 5 monstres sans subir de dégâts", "Collecte 10 objets différents",
			"Parcours 500 blocs à pied", "Sauve 3 animaux", "Ouvre 2 coffres"),
		List.of(
			"Derrota a 25 zombis", "Derrota a 15 arañas", "Derrota a 10 endermen", "Derrota a un jefe", "Derrota a 20 esqueletos",
			"Pesca 20 objetos", "Pesca 5 criaturas marinas", "Extrae 64 carbón", "Extrae 32 hierro", "Extrae 16 diamantes",
			"Cosecha 100 trigo", "Cosecha 64 zanahorias", "Cosecha 50 calabazas", "Corta 64 madera", "Tala 50 árboles",
			"Vende 10 objetos en el Bazaar", "Compra un objeto en el Bazaar", "Fabrica una herramienta nueva", "Encanta un objeto",
			"Forja un objeto", "Visita la isla de otro jugador", "Coloca 5 minions", "Recoge 100 objetos de minions",
			"Completa 3 misiones", "Resuelve una sala de acertijos", "Mata a 5 mobs sin recibir daño", "Recoge 10 objetos distintos",
			"Camina 500 bloques a pie", "Rescata a 3 animales", "Abre 2 cofres"));

	static {
		// ---- Allgemein
		add("player1", "Spieler 1", "Player 1", "Joueur 1", "Jugador 1");
		add("player2", "Spieler 2", "Player 2", "Joueur 2", "Jugador 2");
		add("onoff.on", "An", "On", "Activé", "Activado");
		add("onoff.off", "Aus", "Off", "Désactivé", "Desactivado");
		add("theme.dark", "Dunkel", "Dark", "Sombre", "Oscuro");
		add("theme.light", "Hell", "Light", "Clair", "Claro");
		add("theme.custom", "Eigene", "Custom", "Perso", "Propio");
		add("theme.custom_long", "Eigene Farben", "Custom colors", "Couleurs perso", "Colores propios");
		add("close", "Schließen", "Close", "Fermer", "Cerrar");

		// ---- Meldungen und Fehler (Spiellogik)
		add("err.nogame", "Es läuft gerade kein Spiel.", "No game is running.", "Aucune partie en cours.",
			"No hay ninguna partida en curso.");
		add("err.over", "Die Runde ist schon vorbei.", "The round is already over.", "La manche est déjà terminée.",
			"La ronda ya ha terminado.");
		add("err.cellrange", "Die Feldnummer muss zwischen 1 und %d liegen.", "The cell number must be between 1 and %d.",
			"Le numéro de case doit être entre 1 et %d.", "El número de casilla debe estar entre 1 y %d.");
		add("err.nosession", "Eröffne zuerst ein Spiel (Menü → Spiel, oder /sbb solo bzw. /sbb host).",
			"Open a game first (menu → Game, or /sbb solo or /sbb host).",
			"Ouvre d'abord une partie (menu → Jeu, ou /sbb solo ou /sbb host).",
			"Abre primero una partida (menú → Juego, o /sbb solo o /sbb host).");
		add("err.hostonly", "Nur der Host kann das Spiel starten.", "Only the host can start the game.",
			"Seul l'hôte peut lancer la partie.", "Solo el anfitrión puede iniciar la partida.");
		add("err.needgoals", "Du brauchst mindestens %d Goals (aktuell %d).", "You need at least %d goals (currently %d).",
			"Il te faut au moins %d objectifs (actuellement %d).", "Necesitas al menos %d objetivos (ahora %d).");
		add("err.nopeer", "Noch kein Mitspieler verbunden.", "No other player connected yet.",
			"Aucun autre joueur connecté pour l'instant.", "Aún no hay otro jugador conectado.");
		add("msg.nohost", "Kein Host mit diesem Code gefunden. Prüfe den Code und versuche es erneut.",
			"No host found with this code. Check the code and try again.",
			"Aucun hôte trouvé avec ce code. Vérifie le code et réessaie.",
			"No se encontró ningún anfitrión con este código. Revisa el código e inténtalo de nuevo.");
		add("msg.win_solo", "Bingo! Reihe geschafft in %s.", "Bingo! Line completed in %s.",
			"Bingo ! Ligne complétée en %s.", "¡Bingo! Línea completada en %s.");
		add("msg.win_you", "Du hast gewonnen! Zeit: %s", "You won! Time: %s", "Tu as gagné ! Temps : %s",
			"¡Has ganado! Tiempo: %s");
		add("msg.win_other", "%s hat gewonnen! Zeit: %s", "%s won! Time: %s", "%s a gagné ! Temps : %s",
			"¡%s ha ganado! Tiempo: %s");
		add("msg.joined", "%s ist beigetreten.", "%s joined.", "%s a rejoint la partie.", "%s se ha unido.");
		add("msg.connected", "Verbunden mit %s. Warte, bis der Host startet.", "Connected to %s. Waiting for the host to start.",
			"Connecté à %s. En attente du lancement par l'hôte.", "Conectado con %s. Esperando a que el anfitrión inicie.");
		add("msg.full", "Das Spiel ist schon voll.", "The game is already full.", "La partie est déjà complète.",
			"La partida ya está llena.");
		add("msg.newround", "Neue Runde gestartet: %dx%d%s.", "New round started: %dx%d%s.",
			"Nouvelle manche lancée : %dx%d%s.", "Nueva ronda iniciada: %dx%d%s.");
		add("msg.hostended", "Der Host hat die Runde beendet.", "The host ended the round.",
			"L'hôte a terminé la manche.", "El anfitrión terminó la ronda.");
		add("net.rate", "Zu viele Nachrichten, bitte kurz warten.", "Too many messages, please wait a moment.",
			"Trop de messages, patiente un instant.", "Demasiados mensajes, espera un momento.");
		add("net.sendhttp", "Senden fehlgeschlagen (HTTP %d).", "Sending failed (HTTP %d).", "Échec de l'envoi (HTTP %d).",
			"Error al enviar (HTTP %d).");
		add("net.sendfail", "Senden fehlgeschlagen: %s", "Sending failed: %s", "Échec de l'envoi : %s", "Error al enviar: %s");
		add("net.reconnect", "Verbindung unterbrochen, versuche es erneut ...", "Connection lost, retrying ...",
			"Connexion perdue, nouvelle tentative ...", "Conexión perdida, reintentando ...");

		// ---- HUD
		add("hud.code", "Party-Code: %s", "Party code: %s", "Code de partie : %s", "Código de partida: %s");
		add("hud.peer_connected", "%s ist verbunden", "%s is connected", "%s est connecté", "%s está conectado");
		add("hud.waiting_peer", "Warte auf Mitspieler ...", "Waiting for a player ...", "En attente d'un joueur ...",
			"Esperando a un jugador ...");
		add("hud.start_hint", "Starten im Menü (/sbb) unter Spiel", "Start in the menu (/sbb) under Game",
			"Lance depuis le menu (/sbb) sous Jeu", "Inicia en el menú (/sbb) en Juego");
		add("hud.join_hint", "Mitspieler: /sbb join %s", "Other player: /sbb join %s", "Autre joueur : /sbb join %s",
			"Otro jugador: /sbb join %s");
		add("hud.connected_to", "Verbunden mit %s", "Connected to %s", "Connecté à %s", "Conectado con %s");
		add("hud.connecting", "Verbinde ...", "Connecting ...", "Connexion ...", "Conectando ...");
		add("hud.wait_start", "Warte auf den Start", "Waiting for the start", "En attente du lancement",
			"Esperando el inicio");
		add("hud.solo", "Solo-Modus", "Solo mode", "Mode solo", "Modo solo");
		add("hud.time", "Zeit %s   %dx%d%s", "Time %s   %dx%d%s", "Temps %s   %dx%d%s", "Tiempo %s   %dx%d%s");
		add("hud.bingo", "Bingo! Zeit %s", "Bingo! Time %s", "Bingo ! Temps %s", "¡Bingo! Tiempo %s");
		add("hud.wins", "%s hat gewonnen! %s", "%s won! %s", "%s a gagné ! %s", "¡%s ha ganado! %s");

		// ---- Board-Fenster
		add("board.title", "Skyblock Bingo - Board", "Skyblock Bingo - Board", "Skyblock Bingo - Tableau",
			"Skyblock Bingo - Tablero");
		add("board.none", "Keine Runde aktiv. Starte im Menü (/sbb) unter Spiel eine Runde.",
			"No round active. Start one in the menu (/sbb) under Game.",
			"Aucune manche active. Lance-en une dans le menu (/sbb) sous Jeu.",
			"Ninguna ronda activa. Inicia una en el menú (/sbb) en Juego.");
		add("board.hover", "Klick: Feld %d abhaken / zurücknehmen", "Click: mark / unmark cell %d",
			"Clic : cocher / décocher la case %d", "Clic: marcar / desmarcar la casilla %d");
		add("board.over", "Runde vorbei. Neue Runde im Menü unter Spiel.", "Round over. Start a new one in the menu under Game.",
			"Manche terminée. Nouvelle manche dans le menu sous Jeu.", "Ronda terminada. Nueva ronda en el menú en Juego.");
		add("board.click", "Klicke auf ein Feld, um es abzuhaken.", "Click a cell to mark it.",
			"Clique sur une case pour la cocher.", "Haz clic en una casilla para marcarla.");

		// ---- Verschieben-Fenster
		add("mover.title", "Skyblock Bingo - HUD verschieben", "Skyblock Bingo - Move HUD",
			"Skyblock Bingo - Déplacer le HUD", "Skyblock Bingo - Mover HUD");
		add("mover.reset", "Zurücksetzen", "Reset", "Réinitialiser", "Restablecer");
		add("mover.done", "Fertig", "Done", "Terminé", "Listo");
		add("mover.theme", "Design: %s", "Theme: %s", "Thème : %s", "Tema: %s");
		add("mover.hint1", "Ziehen: Board verschieben     Mausrad: Größe ändern",
			"Drag: move the board     Mouse wheel: change size",
			"Glisser : déplacer le tableau     Molette : taille",
			"Arrastrar: mover el tablero     Rueda: tamaño");
		add("mover.hint2", "Position %d, %d     Feldgröße %d", "Position %d, %d     Cell size %d",
			"Position %d, %d     Taille des cases %d", "Posición %d, %d     Tamaño de casilla %d");

		// ---- Menue: Reiter
		add("menu.title", "Skyblock Bingo - Einstellungen", "Skyblock Bingo - Settings", "Skyblock Bingo - Réglages",
			"Skyblock Bingo - Ajustes");
		add("tab.general", "Allgemein", "General", "Général", "General");
		add("tab.goals", "Goals", "Goals", "Objectifs", "Objetivos");
		add("tab.lists", "Listen", "Lists", "Listes", "Listas");
		add("tab.design", "Design", "Design", "Thème", "Tema");
		add("tab.game", "Spiel", "Game", "Jeu", "Juego");

		// ---- Menue: Allgemein
		add("gen.size", "Board-Größe: %d x %d", "Board size: %d x %d", "Taille du tableau : %d x %d",
			"Tamaño del tablero: %d x %d");
		add("gen.lockout", "Lockout: %s", "Lockout: %s", "Lockout : %s", "Lockout: %s");
		add("gen.hud", "HUD sichtbar: %s", "HUD visible: %s", "HUD visible : %s", "HUD visible: %s");
		add("gen.invclick", "Klick im Inventar: %s", "Click in inventory: %s", "Clic dans l'inventaire : %s",
			"Clic en el inventario: %s");
		add("gen.lang", "Sprache: %s", "Language: %s", "Langue : %s", "Idioma: %s");
		add("gen.name", "Dein Name:", "Your name:", "Ton nom :", "Tu nombre:");
		add("gen.mover", "HUD verschieben / Größe", "Move HUD / size", "Déplacer le HUD / taille", "Mover HUD / tamaño");
		add("gen.openboard", "Board öffnen (abhaken)", "Open board (mark)", "Ouvrir le tableau (cocher)",
			"Abrir tablero (marcar)");
		add("gen.goals", "Goals: %d (benötigt: %d)", "Goals: %d (needed: %d)", "Objectifs : %d (requis : %d)",
			"Objetivos: %d (necesarios: %d)");
		add("gen.nextround", "Größe und Lockout gelten ab der nächsten Runde.",
			"Size and lockout apply from the next round.",
			"La taille et le lockout s'appliquent dès la prochaine manche.",
			"El tamaño y el lockout se aplican desde la próxima ronda.");
		add("gen.invhint", "Im Inventar (E) kannst du direkt auf das Board klicken.",
			"In the inventory (E) you can click the board directly.",
			"Dans l'inventaire (E), tu peux cliquer directement sur le tableau.",
			"En el inventario (E) puedes hacer clic directamente en el tablero.");

		// ---- Menue: Goals
		add("goals.add", "Hinzufügen", "Add", "Ajouter", "Añadir");
		add("goals.added", "%d Goal(s) hinzugefügt.", "%d goal(s) added.", "%d objectif(s) ajouté(s).",
			"%d objetivo(s) añadido(s).");
		add("goals.examples", "Beispiele laden", "Load examples", "Charger des exemples", "Cargar ejemplos");
		add("goals.example_added", "%d Beispiel-Goals hinzugefügt.", "%d example goals added.",
			"%d objectifs d'exemple ajoutés.", "%d objetivos de ejemplo añadidos.");
		add("goals.clear", "Alle löschen", "Delete all", "Tout supprimer", "Borrar todo");
		add("goals.cleared", "Alle Goals gelöscht.", "All goals deleted.", "Tous les objectifs ont été supprimés.",
			"Todos los objetivos borrados.");
		add("goals.empty", "Noch keine Goals. Oben eintippen (mehrere mit ; trennen).",
			"No goals yet. Type above (separate several with ;).",
			"Aucun objectif. Saisis-en un ci-dessus (sépare-les par ;).",
			"Aún no hay objetivos. Escribe arriba (separa varios con ;).");
		add("goals.page", "Seite %d / %d   Goals: %d / %d", "Page %d / %d   Goals: %d / %d",
			"Page %d / %d   Objectifs : %d / %d", "Página %d / %d   Objetivos: %d / %d");

		// ---- Menue: Listen
		add("lists.save", "Speichern", "Save", "Enregistrer", "Guardar");
		add("lists.load", "Laden", "Load", "Charger", "Cargar");
		add("lists.delete", "Löschen", "Delete", "Supprimer", "Borrar");
		add("lists.noname", "Bitte einen Namen für die Liste eingeben.", "Please enter a name for the list.",
			"Saisis un nom pour la liste.", "Introduce un nombre para la lista.");
		add("lists.nogoals", "Es gibt noch keine Goals zum Speichern.", "There are no goals to save yet.",
			"Il n'y a pas encore d'objectifs à enregistrer.", "Aún no hay objetivos que guardar.");
		add("lists.saved", "Liste '%s' gespeichert (%d Goals).", "List '%s' saved (%d goals).",
			"Liste '%s' enregistrée (%d objectifs).", "Lista '%s' guardada (%d objetivos).");
		add("lists.loaded", "Liste '%s' geladen (%d Goals).", "List '%s' loaded (%d goals).",
			"Liste '%s' chargée (%d objectifs).", "Lista '%s' cargada (%d objetivos).");
		add("lists.deleted", "Liste '%s' gelöscht.", "List '%s' deleted.", "Liste '%s' supprimée.", "Lista '%s' borrada.");
		add("lists.none", "Noch keine Listen. Name eingeben und Speichern klicken.",
			"No lists yet. Enter a name and click Save.",
			"Aucune liste. Saisis un nom et clique sur Enregistrer.",
			"Aún no hay listas. Escribe un nombre y pulsa Guardar.");
		add("lists.page", "Seite %d / %d", "Page %d / %d", "Page %d / %d", "Página %d / %d");
		add("lists.info", "Speichert die aktuellen Goals unter dem Namen.", "Saves the current goals under that name.",
			"Enregistre les objectifs actuels sous ce nom.", "Guarda los objetivos actuales con ese nombre.");

		// ---- Menue: Design
		add("design.hex", "Eigene Farben als Hex-Wert (z. B. FF8800) eintippen.",
			"Type custom colors as a hex value (e.g. FF8800).",
			"Saisis les couleurs perso en hexadécimal (ex. FF8800).",
			"Escribe los colores propios en hexadecimal (p. ej. FF8800).");
		add("color.bg", "Hintergrund", "Background", "Fond", "Fondo");
		add("color.cell", "Feld", "Cell", "Case", "Casilla");
		add("color.text", "Text", "Text", "Texte", "Texto");
		add("color.muted", "Nebentext", "Subtext", "Sous-texte", "Subtexto");
		add("color.border", "Rahmen", "Border", "Bordure", "Borde");

		add("picker.title", "Farbe wählen: %s", "Pick a color: %s", "Choisir la couleur : %s", "Elegir color: %s");
		add("picker.hint", "Klicke oder ziehe im Farbfeld und in der Farbleiste.",
			"Click or drag in the color field and the color bar.",
			"Clique ou glisse dans le champ de couleur et la barre.",
			"Haz clic o arrastra en el campo de color y en la barra.");

		// ---- Menue: Spiel
		add("game.solo", "Alleine spielen", "Play solo", "Jouer seul", "Jugar solo");
		add("game.host", "Party erstellen", "Create party", "Créer une partie", "Crear partida");
		add("game.join", "Beitreten", "Join", "Rejoindre", "Unirse");
		add("game.start", "Runde starten", "Start round", "Lancer la manche", "Iniciar ronda");
		add("game.end", "Runde beenden", "End round", "Terminer la manche", "Terminar ronda");
		add("game.leave", "Verlassen", "Leave", "Quitter", "Salir");
		add("game.solo_status", "Solo-Modus. Klicke auf \"Runde starten\".", "Solo mode. Click \"Start round\".",
			"Mode solo. Clique sur « Lancer la manche ».", "Modo solo. Pulsa \"Iniciar ronda\".");
		add("game.host_status", "Party-Code: %s (der Mitspieler gibt ihn bei \"Beitreten\" ein)",
			"Party code: %s (the other player enters it under \"Join\")",
			"Code de partie : %s (l'autre joueur le saisit sous « Rejoindre »)",
			"Código de partida: %s (el otro jugador lo introduce en \"Unirse\")");
		add("game.code_len", "Der Party-Code hat 6 Zeichen.", "The party code has 6 characters.",
			"Le code de partie comporte 6 caractères.", "El código de partida tiene 6 caracteres.");
		add("game.joining", "Verbinde mit Party %s ...", "Connecting to party %s ...", "Connexion à la partie %s ...",
			"Conectando con la partida %s ...");
		add("game.ended", "Runde beendet.", "Round ended.", "Manche terminée.", "Ronda terminada.");
		add("game.left", "Spiel verlassen.", "Left the game.", "Partie quittée.", "Has salido de la partida.");
		add("game.code", "Code:", "Code:", "Code :", "Código:");
		add("game.mode", "Modus: %s", "Mode: %s", "Mode : %s", "Modo: %s");
		add("mode.none", "kein Spiel", "no game", "aucune partie", "sin partida");
		add("mode.solo", "Solo", "Solo", "Solo", "Solo");
		add("mode.host", "Host", "Host", "Hôte", "Anfitrión");
		add("mode.guest", "Mitspieler", "Guest", "Invité", "Invitado");
		add("game.running", "Runde läuft: %dx%d", "Round running: %dx%d", "Manche en cours : %dx%d", "Ronda en curso: %dx%d");
		add("game.norunning", "Keine Runde aktiv", "No round active", "Aucune manche active", "Ninguna ronda activa");

		// ---- Befehle
		add("cmd.goals_added", "%d Goal(s) hinzugefügt. Gesamt: %d / %d benötigt.", "%d goal(s) added. Total: %d / %d needed.",
			"%d objectif(s) ajouté(s). Total : %d / %d requis.", "%d objetivo(s) añadido(s). Total: %d / %d necesarios.");
		add("cmd.only_goals", "Es gibt nur %d Goals.", "There are only %d goals.", "Il n'y a que %d objectifs.",
			"Solo hay %d objetivos.");
		add("cmd.removed", "Entfernt: %s", "Removed: %s", "Supprimé : %s", "Eliminado: %s");
		add("cmd.no_goals_list", "Noch keine Goals. Lege welche an mit /sbb goal add <text> oder /sbb goal example.",
			"No goals yet. Create some with /sbb goal add <text> or /sbb goal example.",
			"Aucun objectif. Crée-en avec /sbb goal add <texte> ou /sbb goal example.",
			"Aún no hay objetivos. Crea alguno con /sbb goal add <texto> o /sbb goal example.");
		add("cmd.goals_header", "Goals (Seite %d/%d, gesamt %d, benötigt %d):", "Goals (page %d/%d, total %d, needed %d):",
			"Objectifs (page %d/%d, total %d, requis %d) :", "Objetivos (página %d/%d, total %d, necesarios %d):");
		add("cmd.goals_cleared", "Alle Goals gelöscht.", "All goals deleted.", "Tous les objectifs ont été supprimés.",
			"Todos los objetivos borrados.");
		add("cmd.examples_added", "%d Beispiel-Goals hinzugefügt. Gesamt: %d", "%d example goals added. Total: %d",
			"%d objectifs d'exemple ajoutés. Total : %d", "%d objetivos de ejemplo añadidos. Total: %d");
		add("cmd.lists_none", "Noch keine Listen gespeichert. Speichern mit /sbb list save <name>",
			"No lists saved yet. Save with /sbb list save <name>",
			"Aucune liste enregistrée. Enregistre avec /sbb list save <nom>",
			"Aún no hay listas guardadas. Guarda con /sbb list save <nombre>");
		add("cmd.lists_header", "Gespeicherte Listen:", "Saved lists:", "Listes enregistrées :", "Listas guardadas:");
		add("cmd.lists_entry", " - %s (%d Goals)", " - %s (%d goals)", " - %s (%d objectifs)", " - %s (%d objetivos)");
		add("cmd.list_missing", "Liste '%s' gibt es nicht. Mit /sbb list show siehst du alle Listen.",
			"List '%s' does not exist. /sbb list show shows all lists.",
			"La liste '%s' n'existe pas. /sbb list show affiche toutes les listes.",
			"La lista '%s' no existe. /sbb list show muestra todas las listas.");
		add("cmd.list_missing2", "Liste '%s' gibt es nicht.", "List '%s' does not exist.", "La liste '%s' n'existe pas.",
			"La lista '%s' no existe.");
		add("cmd.size", "Board-Größe: %dx%d (%d Goals benötigt). Gilt ab der nächsten Runde.",
			"Board size: %dx%d (%d goals needed). Applies from the next round.",
			"Taille du tableau : %dx%d (%d objectifs requis). S'applique dès la prochaine manche.",
			"Tamaño del tablero: %dx%d (%d objetivos necesarios). Se aplica desde la próxima ronda.");
		add("cmd.lockout", "Lockout: %s. Gilt ab der nächsten Runde.", "Lockout: %s. Applies from the next round.",
			"Lockout : %s. S'applique dès la prochaine manche.", "Lockout: %s. Se aplica desde la próxima ronda.");
		add("cmd.name", "Dein Name: %s", "Your name: %s", "Ton nom : %s", "Tu nombre: %s");
		add("cmd.color_part", "Der Teil muss einer von %s sein.", "The part must be one of %s.",
			"La partie doit être l'une de %s.", "La parte debe ser una de %s.");
		add("cmd.color_hex", "Gib die Farbe als 6-stelligen Hex-Wert an, z. B. FF8800.",
			"Give the color as a 6-digit hex value, e.g. FF8800.",
			"Indique la couleur en hexadécimal à 6 chiffres, ex. FF8800.",
			"Indica el color como valor hexadecimal de 6 dígitos, p. ej. FF8800.");
		add("cmd.color_set", "Farbe '%s' = #%s (Design: eigene Farben).", "Color '%s' = #%s (theme: custom colors).",
			"Couleur '%s' = #%s (thème : couleurs perso).", "Color '%s' = #%s (tema: colores propios).");
		add("cmd.theme", "Design: %s", "Theme: %s", "Thème : %s", "Tema: %s");
		add("cmd.theme_custom_hint", " (Farben ändern mit /sbb color <teil> <hex>)",
			" (change colors with /sbb color <part> <hex>)", " (change les couleurs avec /sbb color <partie> <hex>)",
			" (cambia los colores con /sbb color <parte> <hex>)");
		add("cmd.hud_shown", "HUD sichtbar.", "HUD visible.", "HUD visible.", "HUD visible.");
		add("cmd.hud_hidden", "HUD versteckt.", "HUD hidden.", "HUD masqué.", "HUD oculto.");
		add("cmd.hud_pos", "HUD-Position: %d, %d", "HUD position: %d, %d", "Position du HUD : %d, %d",
			"Posición del HUD: %d, %d");
		add("cmd.hud_cell", "Feldgröße: %d", "Cell size: %d", "Taille des cases : %d", "Tamaño de casilla: %d");
		add("cmd.solo", "Solo-Modus. Starte mit /sbb start.", "Solo mode. Start with /sbb start.",
			"Mode solo. Lance avec /sbb start.", "Modo solo. Inicia con /sbb start.");
		add("cmd.host", "Party-Code: %s  (der Mitspieler tippt /sbb join %s)",
			"Party code: %s  (the other player types /sbb join %s)",
			"Code de partie : %s  (l'autre joueur tape /sbb join %s)",
			"Código de partida: %s  (el otro jugador escribe /sbb join %s)");
		add("cmd.started", "Runde gestartet. Hake Felder im Board-Fenster (/sbb board) oder mit /sbb mark <nr> ab.",
			"Round started. Mark cells in the board window (/sbb board) or with /sbb mark <nr>.",
			"Manche lancée. Coche les cases dans la fenêtre du tableau (/sbb board) ou avec /sbb mark <n°>.",
			"Ronda iniciada. Marca casillas en la ventana del tablero (/sbb board) o con /sbb mark <nº>.");
		add("cmd.status", "Modus: %s%s, Goals: %d / %d, Board %dx%d%s, Design: %s, Sprache: %s",
			"Mode: %s%s, goals: %d / %d, board %dx%d%s, theme: %s, language: %s",
			"Mode : %s%s, objectifs : %d / %d, tableau %dx%d%s, thème : %s, langue : %s",
			"Modo: %s%s, objetivos: %d / %d, tablero %dx%d%s, tema: %s, idioma: %s");
		add("cmd.lang_unknown", "Unbekannte Sprache. Verfügbar: de, en, fr, es.", "Unknown language. Available: de, en, fr, es.",
			"Langue inconnue. Disponibles : de, en, fr, es.", "Idioma desconocido. Disponibles: de, en, fr, es.");
		add("cmd.lang_set", "Sprache: %s", "Language: %s", "Langue : %s", "Idioma: %s");

		// ---- Hilfe
		add("help.title", "Befehle (/sbb ...):", "Commands (/sbb ...):", "Commandes (/sbb ...) :", "Comandos (/sbb ...):");
		add("help.menu", " (ohne Zusatz) oder menu  - Einstellungsmenü, hier ist alles einstellbar",
			" (alone) or menu  - settings menu, everything can be set here",
			" (seul) ou menu  - menu des réglages, tout s'y règle",
			" (solo) o menu  - menú de ajustes, aquí se configura todo");
		add("help.board", " board  - Board-Fenster, Felder per Mausklick abhaken",
			" board  - board window, mark cells with a mouse click",
			" board  - fenêtre du tableau, coche les cases d'un clic",
			" board  - ventana del tablero, marca casillas con un clic");
		add("help.gui", " gui  - Fenster zum Verschieben (ziehen) und Skalieren (Mausrad) des Boards",
			" gui  - window to move (drag) and scale (mouse wheel) the board",
			" gui  - fenêtre pour déplacer (glisser) et redimensionner (molette) le tableau",
			" gui  - ventana para mover (arrastrar) y escalar (rueda) el tablero");
		add("help.game", " solo | host | join <code> | start | end | leave | status",
			" solo | host | join <code> | start | end | leave | status",
			" solo | host | join <code> | start | end | leave | status",
			" solo | host | join <code> | start | end | leave | status");
		add("help.mark", " mark <nr>  - Feld abhaken oder wieder entfernen (die Nummer steht im Feld)",
			" mark <nr>  - mark or unmark a cell (the number is shown in the cell)",
			" mark <n°>  - cocher ou décocher une case (le numéro est dans la case)",
			" mark <nº>  - marcar o desmarcar una casilla (el número está en la casilla)");
		add("help.goal", " goal add <text> (mehrere mit ; trennen) | goal remove <nr> | goal list [seite] | goal clear | goal example",
			" goal add <text> (separate several with ;) | goal remove <nr> | goal list [page] | goal clear | goal example",
			" goal add <texte> (sépare-les par ;) | goal remove <n°> | goal list [page] | goal clear | goal example",
			" goal add <texto> (separa varios con ;) | goal remove <nº> | goal list [página] | goal clear | goal example");
		add("help.list", " list save|load|delete <name> | list show  - Goal-Listen speichern",
			" list save|load|delete <name> | list show  - save goal lists",
			" list save|load|delete <nom> | list show  - enregistrer des listes d'objectifs",
			" list save|load|delete <nombre> | list show  - guardar listas de objetivos");
		add("help.settings", " size <3-7> | lockout <true|false> | name <name> | lang <de|en|fr|es>",
			" size <3-7> | lockout <true|false> | name <name> | lang <de|en|fr|es>",
			" size <3-7> | lockout <true|false> | name <nom> | lang <de|en|fr|es>",
			" size <3-7> | lockout <true|false> | name <nombre> | lang <de|en|fr|es>");
		add("help.theme", " theme dark|light|custom | color <teil> <hex>",
			" theme dark|light|custom | color <part> <hex>",
			" theme dark|light|custom | color <partie> <hex>",
			" theme dark|light|custom | color <parte> <hex>");
		add("help.hud", " hud show|hide | hud pos <x> <y> | hud cell <px>",
			" hud show|hide | hud pos <x> <y> | hud cell <px>",
			" hud show|hide | hud pos <x> <y> | hud cell <px>",
			" hud show|hide | hud pos <x> <y> | hud cell <px>");
	}
}
