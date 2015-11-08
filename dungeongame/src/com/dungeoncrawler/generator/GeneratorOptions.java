package com.dungeoncrawler.generator;

public class GeneratorOptions {
	public int seed = 0, width = 50, height = 50, doorCount = 10, roomCount = 40, minRoomSize = 3, maxRoomSize = 5;
	public GeneratorOptions( int seed ) {
		this.seed = seed;
	}
}
