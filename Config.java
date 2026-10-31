package dev.skyblockbingo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Gespeicherte Einstellungen und Goal-Listen (config/skyblockbingo.json). */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public List<String> goals = new ArrayList<>();
	public Map<String, List<String>> lists = new LinkedHashMap<>();
	public int size = 5;
	public boolean lockout = false;
	/** "dark", "light" oder "custom" */
	public String theme = "dark";
	/** Eigene Farben als RRGGBB (Schluessel: bg, cell, text, muted, p0, p1, border) */
	public Map<String, String> custom = new LinkedHashMap<>();
	public String name = "";
	public boolean hudVisible = true;
	public int hudX = 6;
	public int hudY = 6;
	/** Gewuenschte Feldgroesse in GUI-Pixeln */
	public int cell = 56;
	/** Sprache: de, en, fr, es */
	public String lang = "de";
	/** Im Inventar (E) direkt auf das Board klicken */
	public boolean inventoryClick = true;
	/** Party im oeffentlichen Lobby-Verzeichnis anzeigen */
	public boolean publicLobby = false;

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("skyblockbingo.json");
	}

	public static Config load() {
		try {
			Path p = path();
			if (Files.exists(p)) {
				Config c = GSON.fromJson(Files.readString(p, StandardCharsets.UTF_8), Config.class);
				if (c != null) {
					c.fix();
					return c;
				}
			}
		} catch (Exception e) {
			System.err.println("[SkyblockBingo] Konfiguration konnte nicht gelesen werden: " + e);
		}
		return new Config();
	}

	private void fix() {
		if (goals == null) goals = new ArrayList<>();
		if (lists == null) lists = new LinkedHashMap<>();
		if (custom == null) custom = new LinkedHashMap<>();
		if (name == null) name = "";
		if (theme == null) theme = "dark";
		if (lang == null || !Lang.CODES.contains(lang)) lang = "de";
		size = Math.max(3, Math.min(7, size));
		cell = Math.max(24, Math.min(100, cell));
	}

	public void save() {
		try {
			Files.writeString(path(), GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[SkyblockBingo] Konfiguration konnte nicht gespeichert werden: " + e);
		}
	}
}
