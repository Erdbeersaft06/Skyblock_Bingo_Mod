package dev.skyblockbingo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Alle Befehle unter /sbb (bewusst nicht /bingo, das gehoert Hypixel). */
public final class BingoCommands {
	static final List<String> EXAMPLES = List.of(
		"Besiege 25 Zombies", "Besiege 15 Spinnen", "Besiege 10 Endermen", "Besiege einen Boss", "Besiege 20 Skelette",
		"Fische 20 Items", "Fische 5 Seemonster", "Baue 64 Kohle ab", "Baue 32 Eisenerz ab", "Baue 16 Diamanten ab",
		"Ernte 100 Weizen", "Ernte 64 Karotten", "Ernte 50 Kuerbisse", "Baue 64 Holz ab", "Faelle 50 Baeume",
		"Verkaufe 10 Items im Bazaar", "Kaufe ein Item im Bazaar", "Craft ein neues Werkzeug", "Verzaubere ein Item",
		"Schmiede ein Item", "Besuche die Insel eines Spielers", "Platziere 5 Minions", "Sammle 100 Minion-Items",
		"Erledige 3 Quests", "Loese einen Raetsel-Raum", "Toete 5 Mobs ohne Schaden", "Sammle 10 verschiedene Items",
		"Lege 500 Bloecke zu Fuss zurueck", "Rette 3 Tiere", "Oeffne 2 Truhen");

	private BingoCommands() {}

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> register(dispatcher));
	}

	// ---------------------------------------------------------------- Hilfen

	private static LiteralArgumentBuilder<FabricClientCommandSource> lit(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	private static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> arg(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}

	private static void say(CommandContext<FabricClientCommandSource> c, String text) {
		c.getSource().sendFeedback(Component.literal("[Bingo] " + text));
	}

	private static void plain(CommandContext<FabricClientCommandSource> c, String text) {
		c.getSource().sendFeedback(Component.literal(text));
	}

	private static CompletableFuture<Suggestions> listNames(CommandContext<FabricClientCommandSource> c, SuggestionsBuilder b) {
		for (String n : Bingo.cfg.lists.keySet()) b.suggest(n);
		return b.buildFuture();
	}

	private static CompletableFuture<Suggestions> colorKeys(CommandContext<FabricClientCommandSource> c, SuggestionsBuilder b) {
		for (String k : Palette.KEYS) b.suggest(k);
		return b.buildFuture();
	}

	private static void saveCfg() {
		Bingo.cfg.save();
	}

	// ---------------------------------------------------------------- Befehle

	private static void register(CommandDispatcher<FabricClientCommandSource> d) {
		LiteralArgumentBuilder<FabricClientCommandSource> root = lit("sbb");

		root.executes(c -> openMenu(c));
		root.then(lit("menu").executes(c -> openMenu(c)));
		root.then(lit("board").executes(c -> openBoard(c)));
		root.then(lit("help").executes(c -> help(c)));

		// --- Goals
		root.then(lit("goal")
			.then(lit("add").then(arg("text", StringArgumentType.greedyString()).executes(c -> {
				String raw = StringArgumentType.getString(c, "text");
				int added = 0;
				for (String part : raw.split(";")) {
					String g = part.trim();
					if (g.isEmpty()) continue;
					if (g.length() > Bingo.MAX_GOAL_LENGTH) g = g.substring(0, Bingo.MAX_GOAL_LENGTH);
					if (!Bingo.cfg.goals.contains(g)) {
						Bingo.cfg.goals.add(g);
						added++;
					}
				}
				saveCfg();
				say(c, added + " Goal(s) hinzugefuegt. Gesamt: " + Bingo.cfg.goals.size() + " / " + Bingo.needed() + " benoetigt.");
				return 1;
			})))
			.then(lit("remove").then(arg("nr", IntegerArgumentType.integer(1)).executes(c -> {
				int nr = IntegerArgumentType.getInteger(c, "nr");
				if (nr > Bingo.cfg.goals.size()) {
					say(c, "Es gibt nur " + Bingo.cfg.goals.size() + " Goals.");
					return 0;
				}
				String removed = Bingo.cfg.goals.remove(nr - 1);
				saveCfg();
				say(c, "Entfernt: " + removed);
				return 1;
			})))
			.then(lit("list")
				.executes(c -> listGoals(c, 1))
				.then(arg("seite", IntegerArgumentType.integer(1)).executes(c -> listGoals(c, IntegerArgumentType.getInteger(c, "seite")))))
			.then(lit("clear").executes(c -> {
				Bingo.cfg.goals.clear();
				saveCfg();
				say(c, "Alle Goals geloescht.");
				return 1;
			}))
			.then(lit("example").executes(c -> {
				int added = 0;
				for (String g : EXAMPLES) {
					if (!Bingo.cfg.goals.contains(g)) {
						Bingo.cfg.goals.add(g);
						added++;
					}
				}
				saveCfg();
				say(c, added + " Beispiel-Goals hinzugefuegt. Gesamt: " + Bingo.cfg.goals.size());
				return 1;
			})));

		// --- Gespeicherte Listen
		root.then(lit("list")
			.then(lit("save").then(arg("name", StringArgumentType.word()).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				if (Bingo.cfg.goals.isEmpty()) {
					say(c, "Es gibt noch keine Goals zum Speichern.");
					return 0;
				}
				Bingo.cfg.lists.put(n, new java.util.ArrayList<>(Bingo.cfg.goals));
				saveCfg();
				say(c, "Liste '" + n + "' gespeichert (" + Bingo.cfg.goals.size() + " Goals).");
				return 1;
			})))
			.then(lit("load").then(arg("name", StringArgumentType.word()).suggests(BingoCommands::listNames).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				List<String> l = Bingo.cfg.lists.get(n);
				if (l == null) {
					say(c, "Liste '" + n + "' gibt es nicht. Mit /sbb list show siehst du alle Listen.");
					return 0;
				}
				Bingo.cfg.goals = new java.util.ArrayList<>(l);
				saveCfg();
				say(c, "Liste '" + n + "' geladen (" + l.size() + " Goals).");
				return 1;
			})))
			.then(lit("delete").then(arg("name", StringArgumentType.word()).suggests(BingoCommands::listNames).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				if (Bingo.cfg.lists.remove(n) == null) {
					say(c, "Liste '" + n + "' gibt es nicht.");
					return 0;
				}
				saveCfg();
				say(c, "Liste '" + n + "' geloescht.");
				return 1;
			})))
			.then(lit("show").executes(c -> {
				if (Bingo.cfg.lists.isEmpty()) {
					say(c, "Noch keine Listen gespeichert. Speichern mit /sbb list save <name>");
				} else {
					say(c, "Gespeicherte Listen:");
					Bingo.cfg.lists.forEach((k, v) -> plain(c, " - " + k + " (" + v.size() + " Goals)"));
				}
				return 1;
			})));

		// --- Einstellungen
		root.then(lit("size").then(arg("n", IntegerArgumentType.integer(3, 7)).executes(c -> {
			Bingo.cfg.size = IntegerArgumentType.getInteger(c, "n");
			saveCfg();
			say(c, "Board-Groesse: " + Bingo.cfg.size + "x" + Bingo.cfg.size + " (" + Bingo.needed() + " Goals benoetigt). Gilt ab der naechsten Runde.");
			return 1;
		})));
		root.then(lit("lockout").then(arg("an", BoolArgumentType.bool()).executes(c -> {
			Bingo.cfg.lockout = BoolArgumentType.getBool(c, "an");
			saveCfg();
			say(c, "Lockout: " + (Bingo.cfg.lockout ? "an" : "aus") + ". Gilt ab der naechsten Runde.");
			return 1;
		})));
		root.then(lit("name").then(arg("name", StringArgumentType.greedyString()).executes(c -> {
			String n = StringArgumentType.getString(c, "name").trim();
			if (n.length() > 20) n = n.substring(0, 20);
			Bingo.cfg.name = n;
			saveCfg();
			say(c, "Dein Name: " + n);
			return 1;
		})));

		// --- Design
		root.then(lit("theme")
			.then(lit("dark").executes(c -> setTheme(c, "dark")))
			.then(lit("light").executes(c -> setTheme(c, "light")))
			.then(lit("custom").executes(c -> setTheme(c, "custom"))));
		root.then(lit("color")
			.then(arg("teil", StringArgumentType.word()).suggests(BingoCommands::colorKeys)
				.then(arg("hex", StringArgumentType.word()).executes(c -> {
					String key = StringArgumentType.getString(c, "teil");
					String hex = StringArgumentType.getString(c, "hex");
					if (hex.startsWith("#")) hex = hex.substring(1);
					if (!Palette.KEYS.contains(key)) {
						say(c, "Teil muss eines von " + Palette.KEYS + " sein.");
						return 0;
					}
					if (!hex.matches("[0-9a-fA-F]{6}")) {
						say(c, "Farbe als 6-stelliger Hex-Wert angeben, z. B. FF8800.");
						return 0;
					}
					ensureCustom();
					Bingo.cfg.custom.put(key, hex.toUpperCase());
					Bingo.cfg.theme = "custom";
					saveCfg();
					say(c, "Farbe '" + key + "' = #" + hex.toUpperCase() + " (Design: custom).");
					return 1;
				}))));

		// --- HUD-Fenster zum Verschieben und Skalieren
		root.then(lit("gui").executes(c -> openMover(c)));
		root.then(lit("move").executes(c -> openMover(c)));

		// --- HUD
		root.then(lit("hud")
			.then(lit("show").executes(c -> {
				Bingo.cfg.hudVisible = true;
				saveCfg();
				say(c, "HUD sichtbar.");
				return 1;
			}))
			.then(lit("hide").executes(c -> {
				Bingo.cfg.hudVisible = false;
				saveCfg();
				say(c, "HUD versteckt.");
				return 1;
			}))
			.then(lit("pos").then(arg("x", IntegerArgumentType.integer(0, 4000))
				.then(arg("y", IntegerArgumentType.integer(0, 4000)).executes(c -> {
					Bingo.cfg.hudX = IntegerArgumentType.getInteger(c, "x");
					Bingo.cfg.hudY = IntegerArgumentType.getInteger(c, "y");
					saveCfg();
					say(c, "HUD-Position: " + Bingo.cfg.hudX + ", " + Bingo.cfg.hudY);
					return 1;
				}))))
			.then(lit("cell").then(arg("px", IntegerArgumentType.integer(24, 100)).executes(c -> {
				Bingo.cfg.cell = IntegerArgumentType.getInteger(c, "px");
				saveCfg();
				say(c, "Feldgroesse: " + Bingo.cfg.cell + " (wird bei kleinen Fenstern automatisch verkleinert).");
				return 1;
			}))));

		// --- Spiel
		root.then(lit("solo").executes(c -> {
			Bingo.startSolo();
			say(c, "Solo-Modus. Starte mit /sbb start.");
			return 1;
		}));
		root.then(lit("host").executes(c -> {
			String code = Bingo.startHost();
			say(c, "Party-Code: " + code + "  (Mitspieler tippt /sbb join " + code + ")");
			return 1;
		}));
		root.then(lit("join").then(arg("code", StringArgumentType.word()).executes(c -> {
			String code = StringArgumentType.getString(c, "code").trim();
			if (code.length() != 6) {
				say(c, "Der Party-Code hat 6 Zeichen.");
				return 0;
			}
			Bingo.join(code);
			say(c, "Verbinde mit Party " + code.toUpperCase() + " ...");
			return 1;
		})));
		root.then(lit("start").executes(c -> {
			String err = Bingo.startGame();
			if (err != null) {
				say(c, err);
				return 0;
			}
			say(c, "Runde gestartet. Felder abhaken mit /sbb mark <nr>.");
			return 1;
		}));
		root.then(lit("mark").then(arg("nr", IntegerArgumentType.integer(1, 49)).executes(c -> {
			String err = Bingo.mark(IntegerArgumentType.getInteger(c, "nr") - 1);
			if (err != null) {
				say(c, err);
				return 0;
			}
			return 1;
		})));
		root.then(lit("end").executes(c -> {
			Bingo.endGame();
			say(c, "Runde beendet.");
			return 1;
		}));
		root.then(lit("leave").executes(c -> {
			Bingo.leave();
			say(c, "Spiel verlassen.");
			return 1;
		}));
		root.then(lit("status").executes(c -> {
			say(c, "Rolle: " + Bingo.role + (Bingo.code.isEmpty() ? "" : ", Code " + Bingo.code)
				+ ", Goals: " + Bingo.cfg.goals.size() + " / " + Bingo.needed()
				+ ", Board " + Bingo.cfg.size + "x" + Bingo.cfg.size + (Bingo.cfg.lockout ? " Lockout" : "")
				+ ", Design " + Bingo.cfg.theme);
			return 1;
		}));

		d.register(root);
	}

	private static int openMover(CommandContext<FabricClientCommandSource> c) {
		Bingo.cfg.hudVisible = true;
		Bingo.open(MoverScreen::new);
		return 1;
	}

	private static int openMenu(CommandContext<FabricClientCommandSource> c) {
		Bingo.open(ConfigScreen::new);
		return 1;
	}

	private static int openBoard(CommandContext<FabricClientCommandSource> c) {
		Bingo.open(BoardScreen::new);
		return 1;
	}

	private static int help(CommandContext<FabricClientCommandSource> c) {
		say(c, "Befehle (/sbb ...):");
		plain(c, " (ohne Zusatz) oder menu  - Einstellungsmenue, hier ist alles einstellbar");
		plain(c, " board  - Board-Fenster, Felder per Mausklick abhaken");
		plain(c, " gui  - Fenster zum Verschieben (ziehen) und Skalieren (Mausrad) des Boards");
		plain(c, " solo | host | join <code> | start | end | leave | status");
		plain(c, " mark <nr>  - Feld abhaken oder wieder entfernen (Nummer steht im Feld)");
		plain(c, " goal add <text>  (mehrere mit ; trennen) | goal remove <nr> | goal list [seite] | goal clear | goal example");
		plain(c, " list save|load|delete <name> | list show  - Goal-Listen speichern");
		plain(c, " size <3-7> | lockout <true|false> | name <name>");
		plain(c, " theme dark|light|custom | color <teil> <hex>");
		plain(c, " hud show|hide | hud pos <x> <y> | hud cell <px>");
		return 1;
	}

	private static int listGoals(CommandContext<FabricClientCommandSource> c, int page) {
		List<String> goals = Bingo.cfg.goals;
		if (goals.isEmpty()) {
			say(c, "Noch keine Goals. Mit /sbb goal add <text> oder /sbb goal example anlegen.");
			return 1;
		}
		int per = 10;
		int pages = (goals.size() + per - 1) / per;
		int p = Math.min(page, pages);
		say(c, "Goals (Seite " + p + "/" + pages + ", gesamt " + goals.size() + ", benoetigt " + Bingo.needed() + "):");
		for (int i = (p - 1) * per; i < Math.min(goals.size(), p * per); i++) {
			plain(c, " " + (i + 1) + ". " + goals.get(i));
		}
		return 1;
	}

	private static void ensureCustom() {
		if (Bingo.cfg.custom.isEmpty()) {
			Palette cur = Palette.of(Bingo.cfg);
			for (String k : Palette.KEYS) Bingo.cfg.custom.put(k, Palette.hexOf(cur, k));
		}
	}

	private static int setTheme(CommandContext<FabricClientCommandSource> c, String theme) {
		if (theme.equals("custom")) ensureCustom();
		Bingo.cfg.theme = theme;
		saveCfg();
		say(c, "Design: " + theme + (theme.equals("custom") ? " (Farben aendern mit /sbb color <teil> <hex>)" : ""));
		return 1;
	}
}
