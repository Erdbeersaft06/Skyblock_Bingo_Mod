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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Alle Befehle unter /sbb (bewusst nicht /bingo, das gehoert Hypixel). */
public final class BingoCommands {
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

	private static CompletableFuture<Suggestions> langCodes(CommandContext<FabricClientCommandSource> c, SuggestionsBuilder b) {
		for (String k : Lang.CODES) b.suggest(k);
		return b.buildFuture();
	}

	private static void saveCfg() {
		Bingo.cfg.save();
	}

	private static String onOff(boolean b) {
		return Lang.t(b ? "onoff.on" : "onoff.off");
	}

	// ---------------------------------------------------------------- Befehle

	private static void register(CommandDispatcher<FabricClientCommandSource> d) {
		LiteralArgumentBuilder<FabricClientCommandSource> root = lit("sbb");

		root.executes(c -> openMenu());
		root.then(lit("menu").executes(c -> openMenu()));
		root.then(lit("board").executes(c -> {
			Bingo.open(BoardScreen::new);
			return 1;
		}));
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
				say(c, Lang.t("cmd.goals_added", added, Bingo.cfg.goals.size(), Bingo.needed()));
				return 1;
			})))
			.then(lit("remove").then(arg("nr", IntegerArgumentType.integer(1)).executes(c -> {
				int nr = IntegerArgumentType.getInteger(c, "nr");
				if (nr > Bingo.cfg.goals.size()) {
					say(c, Lang.t("cmd.only_goals", Bingo.cfg.goals.size()));
					return 0;
				}
				String removed = Bingo.cfg.goals.remove(nr - 1);
				saveCfg();
				say(c, Lang.t("cmd.removed", removed));
				return 1;
			})))
			.then(lit("list")
				.executes(c -> listGoals(c, 1))
				.then(arg("page", IntegerArgumentType.integer(1)).executes(c -> listGoals(c, IntegerArgumentType.getInteger(c, "page")))))
			.then(lit("clear").executes(c -> {
				Bingo.cfg.goals.clear();
				saveCfg();
				say(c, Lang.t("cmd.goals_cleared"));
				return 1;
			}))
			.then(lit("example").executes(c -> {
				int added = 0;
				for (String g : Lang.examples()) {
					if (!Bingo.cfg.goals.contains(g)) {
						Bingo.cfg.goals.add(g);
						added++;
					}
				}
				saveCfg();
				say(c, Lang.t("cmd.examples_added", added, Bingo.cfg.goals.size()));
				return 1;
			})));

		// --- Gespeicherte Listen
		root.then(lit("list")
			.then(lit("save").then(arg("name", StringArgumentType.word()).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				if (Bingo.cfg.goals.isEmpty()) {
					say(c, Lang.t("lists.nogoals"));
					return 0;
				}
				Bingo.cfg.lists.put(n, new ArrayList<>(Bingo.cfg.goals));
				saveCfg();
				say(c, Lang.t("lists.saved", n, Bingo.cfg.goals.size()));
				return 1;
			})))
			.then(lit("load").then(arg("name", StringArgumentType.word()).suggests(BingoCommands::listNames).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				List<String> l = Bingo.cfg.lists.get(n);
				if (l == null) {
					say(c, Lang.t("cmd.list_missing", n));
					return 0;
				}
				Bingo.cfg.goals = new ArrayList<>(l);
				saveCfg();
				say(c, Lang.t("lists.loaded", n, l.size()));
				return 1;
			})))
			.then(lit("delete").then(arg("name", StringArgumentType.word()).suggests(BingoCommands::listNames).executes(c -> {
				String n = StringArgumentType.getString(c, "name");
				if (Bingo.cfg.lists.remove(n) == null) {
					say(c, Lang.t("cmd.list_missing2", n));
					return 0;
				}
				saveCfg();
				say(c, Lang.t("lists.deleted", n));
				return 1;
			})))
			.then(lit("show").executes(c -> {
				if (Bingo.cfg.lists.isEmpty()) {
					say(c, Lang.t("cmd.lists_none"));
				} else {
					say(c, Lang.t("cmd.lists_header"));
					Bingo.cfg.lists.forEach((k, v) -> plain(c, Lang.t("cmd.lists_entry", k, v.size())));
				}
				return 1;
			})));

		// --- Einstellungen
		root.then(lit("size").then(arg("n", IntegerArgumentType.integer(3, 7)).executes(c -> {
			Bingo.cfg.size = IntegerArgumentType.getInteger(c, "n");
			saveCfg();
			say(c, Lang.t("cmd.size", Bingo.cfg.size, Bingo.cfg.size, Bingo.needed()));
			return 1;
		})));
		root.then(lit("lockout").then(arg("on", BoolArgumentType.bool()).executes(c -> {
			Bingo.cfg.lockout = BoolArgumentType.getBool(c, "on");
			saveCfg();
			say(c, Lang.t("cmd.lockout", onOff(Bingo.cfg.lockout)));
			return 1;
		})));
		root.then(lit("lang").then(arg("code", StringArgumentType.word()).suggests(BingoCommands::langCodes).executes(c -> {
			String code = StringArgumentType.getString(c, "code").toLowerCase();
			if (!Lang.CODES.contains(code)) {
				say(c, Lang.t("cmd.lang_unknown"));
				return 0;
			}
			Bingo.cfg.lang = code;
			saveCfg();
			say(c, Lang.t("cmd.lang_set", Lang.languageName()));
			return 1;
		})));

		// --- Design
		root.then(lit("theme")
			.then(lit("dark").executes(c -> setTheme(c, "dark")))
			.then(lit("light").executes(c -> setTheme(c, "light")))
			.then(lit("custom").executes(c -> setTheme(c, "custom"))));
		root.then(lit("color")
			.then(arg("part", StringArgumentType.word()).suggests(BingoCommands::colorKeys)
				.then(arg("hex", StringArgumentType.word()).executes(c -> {
					String key = StringArgumentType.getString(c, "part");
					String hex = StringArgumentType.getString(c, "hex");
					if (hex.startsWith("#")) hex = hex.substring(1);
					if (!Palette.KEYS.contains(key)) {
						say(c, Lang.t("cmd.color_part", Palette.KEYS.toString()));
						return 0;
					}
					if (!hex.matches("[0-9a-fA-F]{6}")) {
						say(c, Lang.t("cmd.color_hex"));
						return 0;
					}
					Palette.ensureCustom(Bingo.cfg);
					Bingo.cfg.custom.put(key, hex.toUpperCase());
					Bingo.cfg.theme = "custom";
					saveCfg();
					say(c, Lang.t("cmd.color_set", key, hex.toUpperCase()));
					return 1;
				}))));

		// --- HUD-Fenster zum Verschieben und Skalieren
		root.then(lit("gui").executes(c -> openMover()));
		root.then(lit("move").executes(c -> openMover()));

		// --- HUD
		root.then(lit("hud")
			.then(lit("show").executes(c -> {
				Bingo.cfg.hudVisible = true;
				saveCfg();
				say(c, Lang.t("cmd.hud_shown"));
				return 1;
			}))
			.then(lit("hide").executes(c -> {
				Bingo.cfg.hudVisible = false;
				saveCfg();
				say(c, Lang.t("cmd.hud_hidden"));
				return 1;
			}))
			.then(lit("pos").then(arg("x", IntegerArgumentType.integer(0, 4000))
				.then(arg("y", IntegerArgumentType.integer(0, 4000)).executes(c -> {
					Bingo.cfg.hudX = IntegerArgumentType.getInteger(c, "x");
					Bingo.cfg.hudY = IntegerArgumentType.getInteger(c, "y");
					saveCfg();
					say(c, Lang.t("cmd.hud_pos", Bingo.cfg.hudX, Bingo.cfg.hudY));
					return 1;
				}))))
			.then(lit("cell").then(arg("px", IntegerArgumentType.integer(24, 100)).executes(c -> {
				Bingo.cfg.cell = IntegerArgumentType.getInteger(c, "px");
				saveCfg();
				say(c, Lang.t("cmd.hud_cell", Bingo.cfg.cell));
				return 1;
			}))));

		// --- Spiel
		root.then(lit("solo").executes(c -> {
			Bingo.startSolo();
			say(c, Lang.t("cmd.solo"));
			return 1;
		}));
		root.then(lit("host").executes(c -> {
			String code = Bingo.startHost();
			say(c, Lang.t("cmd.host", code, code));
			return 1;
		}));
		root.then(lit("public").then(arg("on", BoolArgumentType.bool()).executes(c -> {
			Bingo.cfg.publicLobby = BoolArgumentType.getBool(c, "on");
			saveCfg();
			say(c, Lang.t("cmd.public", onOff(Bingo.cfg.publicLobby)));
			return 1;
		})));
		root.then(lit("lobbies").executes(c -> {
			Bingo.open(() -> new LobbyScreen(null));
			return 1;
		}));
		root.then(lit("join").then(arg("code", StringArgumentType.word()).executes(c -> {
			String code = StringArgumentType.getString(c, "code").trim();
			if (code.length() != 6) {
				say(c, Lang.t("game.code_len"));
				return 0;
			}
			Bingo.join(code);
			say(c, Lang.t("game.joining", code.toUpperCase()));
			return 1;
		})));
		root.then(lit("start").executes(c -> {
			String err = Bingo.startGame();
			if (err != null) {
				say(c, err);
				return 0;
			}
			say(c, Lang.t("cmd.started"));
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
			say(c, Lang.t("game.ended"));
			return 1;
		}));
		root.then(lit("leave").executes(c -> {
			Bingo.leave();
			say(c, Lang.t("game.left"));
			return 1;
		}));
		root.then(lit("status").executes(c -> {
			String mode = Lang.t(switch (Bingo.role) {
				case NONE -> "mode.none";
				case SOLO -> "mode.solo";
				case HOST -> "mode.host";
				case GUEST -> "mode.guest";
			});
			say(c, Lang.t("cmd.status", mode, Bingo.code.isEmpty() ? "" : " (" + Bingo.code + ")",
				Bingo.cfg.goals.size(), Bingo.needed(), Bingo.cfg.size, Bingo.cfg.size,
				Bingo.cfg.lockout ? " Lockout" : "", Bingo.cfg.theme, Lang.languageName()));
			return 1;
		}));

		d.register(root);
	}

	private static int openMover() {
		Bingo.cfg.hudVisible = true;
		Bingo.open(MoverScreen::new);
		return 1;
	}

	private static int openMenu() {
		Bingo.open(ConfigScreen::new);
		return 1;
	}

	private static int help(CommandContext<FabricClientCommandSource> c) {
		say(c, Lang.t("help.title"));
		for (String k : List.of("help.menu", "help.board", "help.gui", "help.game", "help.mark", "help.goal",
			"help.list", "help.settings", "help.theme", "help.hud")) {
			plain(c, Lang.t(k));
		}
		return 1;
	}

	private static int listGoals(CommandContext<FabricClientCommandSource> c, int page) {
		List<String> goals = Bingo.cfg.goals;
		if (goals.isEmpty()) {
			say(c, Lang.t("cmd.no_goals_list"));
			return 1;
		}
		int per = 10;
		int pages = (goals.size() + per - 1) / per;
		int p = Math.min(page, pages);
		say(c, Lang.t("cmd.goals_header", p, pages, goals.size(), Bingo.needed()));
		for (int i = (p - 1) * per; i < Math.min(goals.size(), p * per); i++) {
			plain(c, " " + (i + 1) + ". " + goals.get(i));
		}
		return 1;
	}

	private static int setTheme(CommandContext<FabricClientCommandSource> c, String theme) {
		if (theme.equals("custom")) Palette.ensureCustom(Bingo.cfg);
		Bingo.cfg.theme = theme;
		saveCfg();
		String name = Lang.t(theme.equals("light") ? "theme.light" : theme.equals("custom") ? "theme.custom_long" : "theme.dark");
		say(c, Lang.t("cmd.theme", name) + (theme.equals("custom") ? Lang.t("cmd.theme_custom_hint") : ""));
		return 1;
	}
}
