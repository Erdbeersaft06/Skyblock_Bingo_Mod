package dev.skyblockbingo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Spiellogik und Multiplayer-Ablauf. Alles hier laeuft auf dem Client-Thread. */
public final class Bingo {
	public enum Role { NONE, SOLO, HOST, GUEST }

	public static final int MAX_GOAL_LENGTH = 50;
	private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final SecureRandom RNG = new SecureRandom();

	public static Config cfg = new Config();
	public static Role role = Role.NONE;
	public static int myIdx = 0;
	public static String code = "";
	public static Game game = null;
	public static final String[] names = {"Spieler 1", "Spieler 2"};
	/** Host: Mitspieler verbunden. Gast: Host hat geantwortet. */
	public static boolean connected = false;

	private static final String MY_ID = randomString(8, "abcdefghijklmnopqrstuvwxyz0123456789");
	private static Net net;
	private static String guestId = null;
	private static int round = 0;
	private static long joinDeadline = 0;

	private Bingo() {}

	// ---------------------------------------------------------------- Hilfen

	private static String randomString(int len, String chars) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) sb.append(chars.charAt(RNG.nextInt(chars.length())));
		return sb.toString();
	}

	/** Nachricht im Chat anzeigen (von jedem Thread aus). */
	public static void chat(String text) {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			if (mc.player != null) {
				mc.player.displayClientMessage(Component.literal("[Bingo] " + text), false);
			}
		});
	}

	private static String myName() {
		return cfg.name.isBlank() ? (role == Role.GUEST ? "Spieler 2" : "Spieler 1") : cfg.name;
	}

	public static int needed() {
		return cfg.size * cfg.size;
	}

	// ---------------------------------------------------------------- Sitzung

	public static String startHost() {
		leave();
		role = Role.HOST;
		myIdx = 0;
		names[0] = myName();
		names[1] = "Spieler 2";
		code = randomString(6, CODE_CHARS);
		connected = false;
		guestId = null;
		net = new Net(code, o -> Minecraft.getInstance().execute(() -> handle(o)), Bingo::chat);
		net.start();
		return code;
	}

	public static void startSolo() {
		leave();
		role = Role.SOLO;
		myIdx = 0;
		names[0] = myName();
	}

	public static void join(String c) {
		leave();
		role = Role.GUEST;
		myIdx = 1;
		code = c.toUpperCase();
		names[1] = myName();
		connected = false;
		net = new Net(code, o -> Minecraft.getInstance().execute(() -> handle(o)), Bingo::chat);
		net.start();
		JsonObject hello = msg("hello");
		hello.addProperty("name", names[1]);
		net.send(hello);
		joinDeadline = System.currentTimeMillis() + 15000;
	}

	public static void leave() {
		if (net != null) {
			if (role == Role.HOST && game != null) net.send(msg("lobby"));
			net.close();
			net = null;
		}
		role = Role.NONE;
		game = null;
		code = "";
		connected = false;
		guestId = null;
		joinDeadline = 0;
	}

	/** Wird jeden Client-Tick aufgerufen. */
	public static void tick() {
		if (role == Role.GUEST && !connected && joinDeadline > 0 && System.currentTimeMillis() > joinDeadline) {
			chat("Kein Host mit diesem Code gefunden. Code pruefen und erneut versuchen.");
			leave();
		}
	}

	// ---------------------------------------------------------------- Spiel

	/** Neue Runde starten (Host oder Solo). Gibt eine Fehlermeldung zurueck oder null. */
	public static String startGame() {
		if (role == Role.NONE) return "Erst mit /sbb solo oder /sbb host ein Spiel eroeffnen.";
		if (role == Role.GUEST) return "Nur der Host kann das Spiel starten.";
		if (cfg.goals.size() < needed()) {
			return "Du brauchst mindestens " + needed() + " Goals (aktuell " + cfg.goals.size() + ").";
		}
		if (role == Role.HOST && !connected) return "Noch kein Mitspieler verbunden.";
		List<String> pool = new ArrayList<>(cfg.goals);
		Collections.shuffle(pool, RNG);
		String[] board = pool.subList(0, needed()).toArray(new String[0]);
		round++;
		game = new Game(cfg.size, cfg.lockout, role == Role.SOLO ? 1 : 2, board, round);
		sendStart();
		return null;
	}

	public static void endGame() {
		game = null;
		if (net != null && role == Role.HOST) net.send(msg("lobby"));
	}

	/** Feld (0-basiert) fuer den lokalen Spieler umschalten. */
	public static String mark(int i) {
		if (game == null) return "Es laeuft gerade kein Spiel.";
		if (game.over) return "Die Runde ist schon vorbei.";
		if (i < 0 || i >= game.board.length) return "Feldnummer muss zwischen 1 und " + game.board.length + " liegen.";
		if (role == Role.GUEST) {
			JsonObject o = msg("click");
			o.addProperty("i", i);
			net.send(o);
		} else {
			click(i, myIdx);
		}
		return null;
	}

	private static void click(int i, int p) {
		Game g = game;
		if (g == null || g.over || i < 0 || i >= g.board.length) return;
		boolean[] cell = g.marks[i];
		if (cell[p]) {
			cell[p] = false;
		} else {
			if (g.lockout && cell[1 - p]) return;
			cell[p] = true;
		}
		int[] line = g.findLine(p);
		if (line != null) {
			g.over = true;
			g.winner = p;
			g.line = line;
			g.finalMs = Math.max(0, System.currentTimeMillis() - g.startMs);
			announceWin(g);
		}
		sendState();
	}

	private static void announceWin(Game g) {
		String time = BingoHud.formatTime(g.finalMs);
		if (g.np == 1) {
			chat("Bingo! Reihe geschafft in " + time + ".");
		} else if (g.winner == myIdx) {
			chat("Du hast gewonnen! Zeit: " + time);
		} else {
			chat(names[g.winner] + " hat gewonnen! Zeit: " + time);
		}
	}

	// ---------------------------------------------------------------- Nachrichten

	private static JsonObject msg(String type) {
		JsonObject o = new JsonObject();
		o.addProperty("t", type);
		o.addProperty("from", MY_ID);
		return o;
	}

	private static void sendStart() {
		if (net == null || game == null || role != Role.HOST) return;
		Game g = game;
		JsonObject o = msg("start");
		o.addProperty("r", g.round);
		o.addProperty("size", g.size);
		o.addProperty("lockout", g.lockout);
		o.addProperty("el", g.elapsed());
		JsonArray board = new JsonArray();
		for (String s : g.board) board.add(s);
		o.add("board", board);
		JsonArray n = new JsonArray();
		n.add(names[0]);
		n.add(names[1]);
		o.add("names", n);
		net.send(o);
		sendState();
	}

	private static void sendState() {
		if (net == null || game == null || role != Role.HOST) return;
		Game g = game;
		JsonObject o = msg("st");
		o.addProperty("r", g.round);
		o.addProperty("m", g.encodeMarks());
		o.addProperty("over", g.over);
		o.addProperty("winner", g.winner);
		o.addProperty("fin", g.finalMs);
		JsonArray line = new JsonArray();
		for (int x : g.line) line.add(x);
		o.add("line", line);
		net.send(o);
	}

	private static String str(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
	}

	private static void handle(JsonObject o) {
		try {
			String from = str(o, "from");
			if (from.equals(MY_ID)) return;
			String t = str(o, "t");
			if (role == Role.HOST) handleAsHost(o, t, from);
			else if (role == Role.GUEST) handleAsGuest(o, t);
		} catch (Exception ignored) {
			// kaputte Nachricht ignorieren
		}
	}

	private static void handleAsHost(JsonObject o, String t, String from) {
		if (t.equals("hello")) {
			if (guestId != null && !guestId.equals(from)) {
				JsonObject full = msg("full");
				full.addProperty("to", from);
				net.send(full);
				return;
			}
			boolean first = guestId == null;
			guestId = from;
			String n = str(o, "name");
			if (n.isBlank()) n = "Spieler 2";
			if (n.equals(names[0])) n = n + " (2)";
			names[1] = n.length() > 20 ? n.substring(0, 20) : n;
			connected = true;
			JsonObject w = msg("welcome");
			w.addProperty("to", from);
			w.addProperty("host", names[0]);
			w.addProperty("guest", names[1]);
			net.send(w);
			if (first) chat(names[1] + " ist beigetreten.");
			if (game != null) sendStart();
		} else if (t.equals("click") && from.equals(guestId) && game != null && o.has("i")) {
			click(o.get("i").getAsInt(), 1);
		}
	}

	private static void handleAsGuest(JsonObject o, String t) {
		boolean forMe = MY_ID.equals(str(o, "to"));
		switch (t) {
			case "welcome" -> {
				if (!forMe) return;
				names[0] = str(o, "host");
				names[1] = str(o, "guest");
				if (!connected) chat("Verbunden mit " + names[0] + ". Warte, bis der Host startet.");
				connected = true;
			}
			case "full" -> {
				if (!forMe) return;
				chat("Das Spiel ist schon voll.");
				leave();
			}
			case "start" -> {
				if (!connected) return;
				int size = o.get("size").getAsInt();
				JsonArray b = o.get("board").getAsJsonArray();
				String[] board = new String[b.size()];
				for (int i = 0; i < board.length; i++) board[i] = b.get(i).getAsString();
				if (board.length != size * size) return;
				Game g = new Game(size, o.get("lockout").getAsBoolean(), 2, board, o.get("r").getAsInt());
				g.startMs = System.currentTimeMillis() - o.get("el").getAsLong();
				JsonArray n = o.get("names").getAsJsonArray();
				names[0] = n.get(0).getAsString();
				names[1] = n.get(1).getAsString();
				boolean isNew = game == null || game.round != g.round;
				game = g;
				if (isNew) chat("Neue Runde gestartet: " + size + "x" + size + (g.lockout ? ", Lockout" : "") + ".");
			}
			case "st" -> {
				Game g = game;
				if (g == null || g.round != o.get("r").getAsInt()) return;
				g.decodeMarks(o.get("m").getAsString());
				boolean wasOver = g.over;
				g.over = o.get("over").getAsBoolean();
				g.winner = o.get("winner").getAsInt();
				g.finalMs = o.get("fin").getAsLong();
				JsonArray l = o.get("line").getAsJsonArray();
				int[] line = new int[l.size()];
				for (int i = 0; i < line.length; i++) line[i] = l.get(i).getAsInt();
				g.line = line;
				if (g.over && !wasOver) announceWin(g);
			}
			case "lobby" -> {
				if (game != null) chat("Der Host hat die Runde beendet.");
				game = null;
			}
			default -> { }
		}
	}
}
