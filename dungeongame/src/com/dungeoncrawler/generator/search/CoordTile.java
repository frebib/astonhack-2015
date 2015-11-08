package com.dungeoncrawler.generator.search;

import com.dungeoncrawler.generator.Tile;

public class CoordTile implements IHeuristic<CoordTile> {
    public Tile tile;
    public int x, y;

    public CoordTile(Tile t, int x, int y) {
        this.tile = t;
        this.x = x;
        this.y = y;
    }

    @Override
    public float calculate(CoordTile goal) {
        return (float) Math.abs((this.x - goal.x) + (this.y - goal.y));
    }
}