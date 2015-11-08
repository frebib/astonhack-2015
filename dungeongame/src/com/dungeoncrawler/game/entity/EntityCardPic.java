package com.dungeoncrawler.game.entity;

import java.util.Random;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.dungeoncrawler.generator.Dungeon;
import com.dungeoncrawler.generator.search.CoordTile;

import Application3D.Application3D;
import utils3D.Camera;
import utils3D.Texture;
import utils3D.VertexBuffer;

public class EntityCardPic extends Entity {
	Texture pic;
	VertexBuffer buf;
	float x, y;
	float targetX, targetY;
	float width, height;
	float ticks = 0;
	float scale = 0;
	float direction = 0;
	boolean agro = false;
	boolean targetReached = true;
	EntityPather pather;
	
	
	public EntityCardPic( Texture pic, VertexBuffer buf, float x, float y, float width, float height, Dungeon d, float scale ) {
		this.pic 	= pic;
		this.buf 	= buf;
		this.x   	= x;
		this.y   	= y;
		this.width 	= width;
		this.height = height;
		this.scale  = scale;
		
		Camera cam = Application3D.getApp().getCamera();
		System.out.println("CamX: "+cam.getX()+" CamY:"+cam.getY());
		pather = new EntityPather(d);
		pather.generatePath((int)Math.floor(x/scale), (int)Math.floor(y/scale), (int)Math.floor(cam.getX()/scale), (int)Math.floor(cam.getY()/scale));
	
		if( pather.path != null ) {
			for( int i=0; i < pather.path.size(); i++ ) {
				CoordTile t = pather.path.get(i).getData();
				System.out.println("Path node: "+i+" "+t.x+" "+t.y);
			}
		}
		
		ticks = (float) ((new Random()).nextFloat()*10.0); // <-- lol
	}
	
	@Override
	public void update(){
		ticks += 0.3;
		Camera cam = Application3D.getApp().getCamera();
		
		direction = (float) Math.atan((cam.getY()-y)/(cam.getX()-x));
		
		// Pathfinding
		if( pather.hasPath()) {
			if( targetReached) {
				if( pather.hasNextTarget() ){
					CoordTile ct = pather.getNextTarget();
					targetX = (ct.x)*scale;
					targetY = (ct.y)*scale;
					targetReached = false;
					System.out.println("x: "+x+" y: "+y+" targetx: "+targetX+" targety: "+targetY);
				}else {
					// Update path
					pather.generatePath((int)Math.floor(x/scale), (int)Math.floor(y/scale), (int)Math.floor(cam.getX()/scale), (int)Math.floor(cam.getY()/scale));
				}
			} else {
				// Check 
				
				
				// Move to target
				int target = 0;
				if( Math.abs( x - targetX) > 8 ){
					x += 1*Math.signum(targetX - x);
				}else{
					target ++;
				}
				if( Math.abs( y - targetY) > 8 ) {
					y += 1*Math.signum(targetY - y);
				}else{
					target ++;
				}
				
				if( target >= 2 ) {
					// Reached target
					targetReached = true;
				}
			}
			
		}else{
			System.out.println("NO PATH!");
		}
	}
	
	@Override
	public void render(){
		pic.bind();
		Matrix4f mf = Application3D.getApp().getWorldMatrix();
		mf.setIdentity();
		
		// Translate
		mf.translate(new Vector3f(x, y, 0.0f ));
		
		// Animate
		mf.rotate( (float) (direction + Math.PI/2 + Math.sin(ticks)*0.25f), new Vector3f( 0, 0, 1));
		
		mf.translate( new Vector3f((float) (-Math.sin(ticks)*width*0.25), 0.0f, 0.0f ));
		mf.rotate( (float) (Math.sin(ticks)*0.25f), new Vector3f( 1, 0, 0));
		
		// scale
		mf.scale(new Vector3f( width, 1.0f, height ));
		
		Application3D.getApp().matrixWorldBind();
		buf.render();
		
		///////////////////////////////////////////////////////////////
		// Render Path
		/*if( )
		for( int i=0; i < pather.path.size(); i++ ) {
			CoordTile t = pather.path.get(i).getData();
			
			mf.setIdentity();
			Application3D.getApp().matrixWorldBind();
			//mf.setIdentity();
			//mf.translate(new Vector3f(t.x*scale, t.y*scale, 0.0f ));
			Application3D.getApp().getRenderUtils().drawQuad(t.x*scale, t.y*scale, 4.0f, 8, 8);
			
		}
		*/
		
		
	}
}
