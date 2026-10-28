package dev.skyblockbingo;

import java.util.ArrayList;
import java.util.List;

/** Zustand einer Bingo-Runde. */
public final class Game {
	public final int size;
	public final boolean lockout;
	/** Anzahl Spieler (1 = Solo, 2 = Multiplayer) */
	public final int np;
	public final String[] board;
	/** marks[feld][spieler] */
	public final boolean[][] marks;
	public final int round;

	public boolean over;
	public int winner = -1;
	public int[] line = new int[0];
	public long startMs;
	public long finalMs;

	public Game(int size, boolean lockout, int np, String[] board, int round) {
		this.size = size;
		this.lockout = lockout;
		this.np = np;
		this.board = board;
		this.marks = new boolean[board.length][2];
		this.round = round;
		this.startMs = System.currentTimeMillis();
	}

	public long elapsed() {
		return over ? finalMs : Math.max(0, System.currentTimeMillis() - startMs);
	}

	public int count(int p) {
		int c = 0;
		for (boolean[] m : marks) if (m[p]) c++;
		return c;
	}

	/** Liefert eine komplette Reihe, Spalte oder Diagonale des Spielers oder null. */
	public int[] findLine(int p) {
		int n = size;
		List<int[]> lines = new ArrayList<>();
		for (int r = 0; r < n; r++) {
			int[] l = new int[n];
			for (int c = 0; c < n; c++) l[c] = r * n + c;
			lines.add(l);
		}
		for (int c = 0; c < n; c++) {
			int[] l = new int[n];
			for (int r = 0; r < n; r++) l[r] = r * n + c;
			lines.add(l);
		}
		int[] d1 = new int[n];
		int[] d2 = new int[n];
		for (int i = 0; i < n; i++) {
			d1[i] = i * n + i;
			d2[i] = i * n + (n - 1 - i);
		}
		lines.add(d1);
		lines.add(d2);
		for (int[] l : lines) {
			boolean all = true;
			for (int i : l) {
				if (!marks[i][p]) {
					all = false;
					break;
				}
			}
			if (all) return l;
		}
		return null;
	}

	public boolean inWinLine(int i) {
		for (int x : line) if (x == i) return true;
		return false;
	}

	public String encodeMarks() {
		StringBuilder sb = new StringBuilder(marks.length);
		for (boolean[] m : marks) sb.append((char) ('0' + (m[0] ? 1 : 0) + (m[1] ? 2 : 0)));
		return sb.toString();
	}

	public void decodeMarks(String s) {
		for (int i = 0; i < marks.length && i < s.length(); i++) {
			int v = s.charAt(i) - '0';
			marks[i][0] = (v & 1) != 0;
			marks[i][1] = (v & 2) != 0;
		}
	}
}
