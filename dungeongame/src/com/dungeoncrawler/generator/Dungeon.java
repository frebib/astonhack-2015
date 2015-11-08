package com.dungeoncrawler.generator;


import com.dungeoncrawler.generator.search.*;
import com.dungeoncrawler.generator.search.maybe.Just;
import com.dungeoncrawler.generator.search.maybe.Maybe;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.List;

public class Dungeon {
    public Tile[][] tiles;
    public Rectangle[] rooms;
    public int width, height;
    public Random generator;
    public Node[][] tileNodes;
    public ArrayList<Node<CoordTile>> nodes;
    public ArrayList<Edge> corridors;
    public GeneratorOptions opts;
    public Dungeon(GeneratorOptions opts) {
    	this.opts = opts;
        Generator gen = new Generator(opts);
        generator = gen.rand;

        this.tiles = new Tile[opts.width][opts.height];
        for (int x = 0; x < tiles.length; x++)
            for (int y = 0; y < tiles[0].length; y++)
                tiles[x][y] = Tile.EMPTY;
        
        this.width = opts.width;
        this.height = opts.height;
        this.rooms = gen.genRooms();
        System.out.println(Arrays.toString(rooms));
        gen.addRooms(tiles, rooms);
        gen.genPaths(tiles, rooms);
        generateGrid(true);
        
        // Gen outline boys
        for (int x = 0; x < tiles.length; x++){
        	tiles[x][0] = Tile.WALL;
        	tiles[x][tiles[0].length-1] = Tile.WALL;
        }
        for (int y = 0; y < tiles[0].length; y++){
        	tiles[0][y] = Tile.WALL;
        	tiles[tiles.length-1][y] = Tile.WALL;
        }
        for (int x = 0; x < tiles.length; x++){
        	for (int y = 0; y < tiles[0].length; y++){
        		char c =' ';
        		switch( tiles[x][y] ) {
	        		case WALL:
	        			c = '#';
	        		break;
	        		case ROOM:
	        			c = '.';
	        		break;
	        		case CORRIDOR:
	        			c = '*';
	        		break;
        		}
        		
        		System.out.print(c);
        	}
        	System.out.println();
        }
    }

    private class Generator {
        private GeneratorOptions opts;
        private Random rand;

        protected Generator(GeneratorOptions opts) {
            this.opts = opts;
            rand = new Random(opts.seed == 0 ? System.nanoTime() : opts.seed);
        }

        public Rectangle[] genRooms() {
            Rectangle[] rooms = new Rectangle[opts.roomCount];
            boolean collides;

            for (int i = 0; i < opts.roomCount; i++) {
                Rectangle room;
                do {
                    int min = opts.minRoomSize;
                    int size = opts.maxRoomSize - min + 1;

                    int width = min + rand.nextInt(size), height = min + rand.nextInt(size);
                    room = new Rectangle(rand.nextInt(opts.width - width - 2) + 1,
                            rand.nextInt(opts.height - height - 2) + 1, width, height);

                    collides = false;
                    for (Rectangle r : rooms) {
                        if (r == null)
                            break;
                        if (r.intersects(room)) {
                            collides = true;
                            break;
                        }
                    }
                } while (collides);
                rooms[i] = room;
            }
            return rooms;
        }

        public void addRooms(Tile[][] tiles, Rectangle[] rooms) {
            for (Rectangle r : rooms) {
                for (int x = 0; x < r.width; x++)
                    for (int y = 0; y < r.height; y++) {
                            tiles[x + 1 + r.x][y + 1 + r.y] = Tile.ROOM;
                    }

            }
        }

        public void genPaths(Tile[][] tiles, Rectangle[] rooms) {
            Point2D.Double[] points = new Point2D.Double[rooms.length];
            for (int i = 0; i < rooms.length; i++)
                points[i] = new Point2D.Double(rooms[i].getCenterX(), rooms[i].getCenterY());

            corridors = new ArrayList<>();
            ArrayList<Point2D.Double> conn = new ArrayList<>(),
                    nconn = new ArrayList<>(Arrays.asList(points));
            conn.add(points[0]);
            nconn.remove(points[0]);
            for (Point2D.Double r1 : nconn) {
                Point2D.Double closest = conn.get(0);
                for (Point2D.Double r2 : conn)
                    if (r2 != closest && r1.distance(r2) < r1.distance(closest))
                        closest = r2;

                nconn.remove(closest);
                conn.add(closest);
                corridors.add(new Edge(r1, closest));
            }

            generateGrid(false);
            int w = tiles.length;
            int h = tiles[0].length;
            Search<CoordTile> s = new Search<>(new PriorityQueue<>(), nodes);
            for (Edge c : corridors) {
                Maybe<List<Node<CoordTile>>> maybPath = s.findPathFrom(tileNodes[((int) c.a.x)][((int) c.a.y)],
                        coordTile -> coordTile.equals(tileNodes[((int) c.b.x)][((int) c.b.y)].getData()));

                if (maybPath.isNothing())
                    continue;

                List<Node<CoordTile>> path = maybPath.fromMaybe();
                for (Node<CoordTile> n : path) {
                    CoordTile ct = n.getData();
                    if (tiles[ct.x][ct.y] != Tile.ROOM)
                        tiles[ct.x][ct.y] = Tile.CORRIDOR;
                }
            }
            for (int x = 1; x < w - 1; x++){
                for (int y = 1; y < h - 1; y++) {
                    if (tiles[x][y] != Tile.CORRIDOR)
                        continue;

                    Tile[] neighbours = new Tile[4];
                    neighbours[0] = tiles[x + 1][y];
                    neighbours[1] = tiles[x - 1][y];
                    neighbours[2] = tiles[x][y + 1];
                    neighbours[3] = tiles[x][y - 1];

                    int count = 0;
                    int wallP = 0;
                    for (Tile t : neighbours) {
                        count += t == Tile.ROOM || t == Tile.CORRIDOR ? 1 : 0;
                    }
                    if (count > 2){
                        tiles[x][y] = Tile.ROOM;
                    }
                }
            }
            // Stupid edge case
            for (int x = 1; x < w - 1; x++){
                for (int y = 1; y < h - 1; y++) {
                    if (tiles[x][y] != Tile.CORRIDOR)
                        continue;

                    Tile[] neighbours = new Tile[4];
                    neighbours[0] = tiles[x + 1][y];
                    neighbours[1] = tiles[x - 1][y];
                    neighbours[2] = tiles[x][y + 1];
                    neighbours[3] = tiles[x][y - 1];

                    int count = 0;
                    int wallP = 0;
                    for (Tile t : neighbours) {
                        count += t == Tile.ROOM? 1 : 0;
                        wallP += t == Tile.WALL? 1 : 0;
                    }
                    
                    if (count == 2 && wallP == 2){
                        tiles[x][y] = Tile.ROOM;
                    }
                }
            }
            
            
            for (int x = 0; x < w; x++){
                for (int y = 0; y < h; y++) {
                    if (tiles[x][y] != Tile.EMPTY)
                        continue;

                    if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
                        tiles[x][y] = Tile.WALL;
                        continue;
                    }

                    Tile[] neighbours = new Tile[4];
                    neighbours[0] = tiles[x + 1][y];
                    neighbours[1] = tiles[x - 1][y];
                    neighbours[2] = tiles[x][y + 1];
                    neighbours[3] = tiles[x][y - 1];

                    for (Tile t : neighbours) {
                        if (t == Tile.ROOM || t == Tile.CORRIDOR) {
                            tiles[x][y] = Tile.WALL;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void generateGrid(boolean clip){
    	int w = tiles.length;
        int h = tiles[0].length;
        tileNodes = new Node[w][h];
        // Add nodes
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                tileNodes[x][y] = new Node<>(new CoordTile(tiles[x][y], x, y));

        // Add node children
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++) {
            	if( !clip ) {
	                if (inRange(w, h, x + 1, y))
	                    tileNodes[x][y].addChild(tileNodes[x + 1][y]);
	                if (inRange(w, h, x - 1, y))
	                    tileNodes[x][y].addChild(tileNodes[x - 1][y]);
	                if (inRange(w, h, x, y + 1))
	                    tileNodes[x][y].addChild(tileNodes[x][y + 1]);
	                if (inRange(w, h, x, y - 1))
	                    tileNodes[x][y].addChild(tileNodes[x][y - 1]);
            	}else{
            		Tile t = ((CoordTile)tileNodes[x][y].getData()).tile;
            		if( !t.walkable ) { continue; }
            		if (inRange(w, h, x + 1, y)){
            			Tile ta = ((CoordTile)tileNodes[x+1][y].getData()).tile;
            			if( ta.walkable )
            				tileNodes[x][y].addChild(tileNodes[x + 1][y]);
            		}
	                if (inRange(w, h, x - 1, y)){
	                	Tile ta = ((CoordTile)tileNodes[x-1][y].getData()).tile;
        				if( ta.walkable )
        					tileNodes[x][y].addChild(tileNodes[x -1][y]);
	                }
	                if (inRange(w, h, x, y + 1)){
	                	Tile ta = ((CoordTile)tileNodes[x][y+1].getData()).tile;
        				if( ta.walkable )
        					tileNodes[x][y].addChild(tileNodes[x][y+1]);
	                }
	                if (inRange(w, h, x, y - 1)){
	                	Tile ta = ((CoordTile)tileNodes[x][y-1].getData()).tile;
        				if( ta.walkable )
        					tileNodes[x][y].addChild(tileNodes[x][y-1]);
	                }
		            
		          
            	}
            }

        nodes = new ArrayList<>(w * h);
        for (int x = 0; x < w; x++)
            nodes.addAll(Arrays.asList((Node<CoordTile>[]) tileNodes[x]).subList(0, h));
    }

    private boolean inRange(int width, int height, int x, int y) {
        return (x > 0 && x < width) && (y > 0 && y < height);
    }

    public class Edge {
        public Point2D.Double a;
        public Point2D.Double b;

        public Edge(Point2D.Double a, Point2D.Double b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "Edge[" + a.toString() + ", " + b.toString() + "]";
        }
    }
}