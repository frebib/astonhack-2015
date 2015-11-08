package com.dungeoncrawler.generator;

public enum Tile {
	EMPTY(0), ROOM(1, false, true), CORRIDOR(2, false, true), WALL(3, true, false), DOOR(4, true, true);

	private final int id;
	public final boolean solid;
	public final boolean walkable;

	private Tile(int id) {
		this.id = id;
		this.solid = false;
		this.walkable = false;
	}

	private Tile(int id, boolean solid, boolean walkable) {
		this.id = id;
		this.solid = solid;
		this.walkable = walkable;
	}
}
