package com.dungeoncrawler.game.entity;

import java.util.List;

import com.dungeoncrawler.generator.Dungeon;
import com.dungeoncrawler.generator.Tile;
import com.dungeoncrawler.generator.Dungeon.Edge;
import com.dungeoncrawler.generator.search.CoordTile;
import com.dungeoncrawler.generator.search.Node;
import com.dungeoncrawler.generator.search.PriorityQueue;
import com.dungeoncrawler.generator.search.Search;
import com.dungeoncrawler.generator.search.maybe.Maybe;

public class EntityPather {
	/**
	 * Class responsible for allowing an entity to generate a path and step along it.
	 */
	Dungeon dungeon;
	Search<CoordTile> searcher;
	List<Node<CoordTile>> path;
	int pathIndex = 0;
	
	public EntityPather(Dungeon dungeon){
		this.dungeon = dungeon;
	}
	
	public boolean hasPath(){
		return (path != null)&&!path.isEmpty();
	}
	
	public void generatePath( int x, int y, int targetX, int targetY ){
		searcher = new Search<>(new PriorityQueue<>(), dungeon.nodes);
		System.out.println(x+ " "+y +" "+targetX+" "+targetY);
        Maybe<List<Node<CoordTile>>> maybPath = searcher.findPathFrom(dungeon.tileNodes[x][y],
                coordTile -> coordTile.equals(dungeon.tileNodes[targetX][targetY].getData()));

        if (maybPath.isNothing()){
        	path = null;
        	return;
        }
            

        path = maybPath.fromMaybe();
        pathIndex = 0;
        if( path.size() <= 0 ) {
        	path = null;
        }
	}
	
	public boolean hasNextTarget(){
		return ( pathIndex < path.size());
	}
	
	public CoordTile getNextTarget(){
		if( hasNextTarget()){
			 Node<CoordTile> a = path.get(pathIndex);
			 pathIndex ++;
			 return a.getData();
		}
		return null;
	}
	
}
