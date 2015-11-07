package com.dungeoncrawler.generator;

public enum Tile {
	EMPTY(0), ROOM(1), CORRIDOR(2), WALL(3, true), DOOR(4);

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
