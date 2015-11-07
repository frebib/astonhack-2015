package com.dungeoncrawler.generator;

public enum Tile {
	Empty(0), Room(1), Corridor(2), Wall(3, true), Door(4);

	private final int id;
	public final boolean solid;

	private Tile(int id) {
		this.id = id;
		this.solid = false;
	}

	private Tile(int id, boolean solid) {
		this.id = id;
		this.solid = solid;
	}
}
