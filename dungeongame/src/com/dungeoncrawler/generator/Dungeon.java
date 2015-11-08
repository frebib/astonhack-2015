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

    public Dungeon(GeneratorOptions opts) {
        Generator gen = new Generator(opts);
        generator = gen.rand;

        this.tiles = new Tile[opts.width][opts.height];
        for (int x = 0; x < tiles.length; x++)
            for (int y = 0; y < tiles[0].length; y++)
                tiles[x][y] = Tile.EMPTY;

        this.rooms = gen.genRooms();
        System.out.println(Arrays.toString(rooms));
        gen.addRooms(tiles, rooms);
        gen.genPaths(tiles, rooms);

        this.width = opts.width;
        this.height = opts.height;

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
                for (int x = 0; x < r.width + 2; x++)
                    for (int y = 0; y < r.height + 2; y++) {
                        if (x == 0 || y == 0 || x == r.width + 1 || y == r.height + 1) {
                            if (tiles[x + r.x][y + r.y] == Tile.ROOM)
                                continue;
                            tiles[x + r.x][y + r.y] = Tile.WALL;
                        }
                        else
                            tiles[x + r.x][y + r.y] = Tile.ROOM;
                    }

            }
        }


        public void genPaths(Tile[][] tiles, Rectangle[] rooms) {
            Point2D.Double[] points = new Point2D.Double[rooms.length];
            for (int i = 0; i < rooms.length; i++)
                points[i] = new Point2D.Double(rooms[i].getCenterX(), rooms[i].getCenterY());

            ArrayList<Edge> corridors = new ArrayList<>();
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

            int w = tiles.length;
            int h = tiles[0].length;
            Node[][] tileNodes = new Node[w][h];
            // Add nodes
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    tileNodes[x][y] = new Node<>(new CoordTile(tiles[x][y], x, y));

            // Add node children
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++) {
                    if (inRange(w, h, x + 1, y))
                        tileNodes[x][y].addChild(tileNodes[x + 1][y]);
                    if (inRange(w, h, x - 1, y))
                        tileNodes[x][y].addChild(tileNodes[x - 1][y]);
                    if (inRange(w, h, x, y + 1))
                        tileNodes[x][y].addChild(tileNodes[x][y + 1]);
                    if (inRange(w, h, x, y - 1))
                        tileNodes[x][y].addChild(tileNodes[x][y - 1]);
                }

            ArrayList<Node<CoordTile>> nodes = new ArrayList<>(w * h);
            for (int x = 0; x < w; x++)
                nodes.addAll(Arrays.asList((Node<CoordTile>[]) tileNodes[x]).subList(0, h));

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
        }
    }

    private boolean inRange(int width, int height, int x, int y) {
        return (x > 0 && x < width) && (y > 0 && y < height);
    }

    private class Edge {
        Point2D.Double a, b;

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