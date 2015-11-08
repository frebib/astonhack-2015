package com.dungeoncrawler.game.render;

import Application3D.Application3D;
import Application3D.Renderer3D;
import sun.swing.plaf.WindowsKeybindings;
import utils3D.Camera;
import utils3D.ModelImporter;
import utils3D.RenderUtils;
import utils3D.Texture;
import utils3D.VertexBuffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.dungeoncrawler.game.entity.Entity;
import com.dungeoncrawler.game.entity.EntityCardPic;
import com.dungeoncrawler.generator.Dungeon;
import com.dungeoncrawler.generator.GeneratorOptions;
import com.dungeoncrawler.generator.Tile;
import com.dungeoncrawler.generator.search.IHeuristic;
import com.dungeoncrawler.generator.search.PriorityQueue;
import com.sun.media.sound.ModelChannelMixer;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.wikireader.WikiPage;
import com.wikireader.WikiReader;

public class DungeonScene implements Renderer3D {

	private Application3D app3D = Application3D.getApp();
	private Texture texFloor, texWall, texDungeon, texDetail;
	private VertexBuffer sceneFloors, sceneWalls, sceneDetails, archModel, doorModel, wallModel, floorModel, ceilingModel, candelabraModel, torch1Model;
	
	private Tile[][] map;
	private int mapSize = 30;
	private int scale   = 32; // Size of generated geometry per cell in units.
	private float playerPadding = 10.0f;
	
	private ArrayList<PointLight> lights;
	private ArrayList<Entity> entities;
	private PointLight playerLight;
	private Random generator;
	Dungeon  d;
	float camWobbleBoyes;
	
	Vector3f telepos;
	boolean teleport = false;

	float vspeed = 0f;
	
	boolean placed = false;
	
	@Override
	public void init() {
		// Init
		System.out.println("Creating Dungeon Renderer");
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
		
		generator = new Random();
		lights    = new ArrayList<>();
		entities  = new ArrayList<>();
		
		float spawnX=0, spawnY=0;
		
		// Setup rendering properties
		/*GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);*/
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8);
		
		// ********************************************** //
		// Load shit off wikipedia
		WikiPage wp = null;
		try {
			wp = WikiReader.getPage(/*WikiReader.getRandomPage()*/"Queen_(band)");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("BODIED");
			System.exit(1);
		}
		
		
		
		// *********************************************** //
		// Create map
		d = new Dungeon( new GeneratorOptions(wp.Title.hashCode()));
		
		map = d.tiles;
		mapSize = map.length;
		// place walls in
				for( int i=0; i < d.opts.width; i++ ) {
					for( int j=0; j < d.opts.height; j++ ) {
						
						
						if( i==0 || j == 0 || i == d.opts.width-1 || j == d.opts.height-1 ) {
							map[i][j] = Tile.WALL;
						}else {
							if( map[i][j] == Tile.CORRIDOR || map[i][j] == Tile.ROOM ) {
								Tile up, down, left, right;
								up    = map[i][j-1];
								down  = map[i][j+1];
								left  = map[i-1][j];
								right = map[i+1][j];
								
								if( up == Tile.EMPTY ) {
									map[i][j-1] = Tile.WALL;
								}
								if( down == Tile.EMPTY ) {
									map[i][j+1] = Tile.WALL;
								}
								if( left == Tile.EMPTY ) {
									map[i-1][j] = Tile.WALL;
								}
								if( right == Tile.EMPTY ) {
									map[i+1][j] = Tile.WALL;
								}
							}
						}
					}
				}
				
		// *********************************************** //
				// Slap in SOme DOoors
				int doorsPlaced = 0;
				while( doorsPlaced < 5 ) {
					int x, y;
					x = d.generator.nextInt(d.opts.width-1);
					y = d.generator.nextInt(d.opts.height-1);
					
					int touching = 0;
					try{
						Tile up, down, left, right;
						up    = map[x][y-1];
						down  = map[x][y+1];
						left  = map[x-1][y];
						right = map[x+1][y];
						
						touching = (up==Tile.ROOM?1:0)+(down==Tile.ROOM?1:0)
									+(left==Tile.ROOM?1:0)+(right==Tile.ROOM?1:0);
					}catch( Exception e){
						continue;
					}
					
					if( map[x][y] == Tile.WALL && touching > 0) {
						map[x][y] = Tile.DOOR;
						doorsPlaced++;
					}
				}
				
		
		
		
		// *********************************************** //
		
		// Load in some resources
		texFloor = app3D.getResources().loadTexture("res/textures/brick.png", "texFloor");
		texWall  = app3D.getResources().loadTexture("res/textures/wall.png", "texWall");
		texDungeon = app3D.getResources().loadTexture("res/textures/dungeon.png", "texDungeon");
		texDetail  = app3D.getResources().loadTexture("res/textures/details.png", "texDetails");
		
		archModel    	= ModelImporter.importModel("res/models/Arch.gmmod");
		wallModel    	= ModelImporter.importModel("res/models/Wall.gmmod");
		floorModel   	= ModelImporter.importModel("res/models/Floor.gmmod");
		ceilingModel 	= ModelImporter.importModel("res/models/Ceiling.gmmod");
		candelabraModel = ModelImporter.importModel("res/models/Candelabra.gmmod");
		torch1Model     = ModelImporter.importModel("res/models/Torch1.gmmod");
		doorModel		= ModelImporter.importModel("res/models/ArchDoor.gmmod");
		// Generate block:
		//block = ModelImporter.importModel("res/models/block.gmmod");
		sceneFloors = generateScene_Floors( map );
		sceneWalls  = generateScene_Walls( map );
		sceneDetails = generateScene_Details( map );
		
		//***************************************************************** //
		// Load Entity resources
		VertexBuffer entityModel = ModelImporter.importModel("res/models/Enemy.gmmod");
		
		// Find spawn
		/*for( int i=0; i < mapSize; i++ ) {
			for( int j=0; j < mapSize; j++ ) {*/
				//if( map[i][j].walkable ) {
		
					
		
					spawnX = (float) ((d.rooms[0].getCenterX())*scale);
					spawnY = (float) ((d.rooms[0].getCenterY())*scale);
		/*		}
			}
		}*/
		
		// Set initial position
		app3D.getCamera().setPosition(new Vector3f( spawnX, spawnY, 16.0f ));
		
		//***************************************************************** //
		// Spawn in Entities
		int maxEntities = 30;
		for( int i=0; i < mapSize && maxEntities > 0; i++ ) {
			for( int j=0; j < mapSize && maxEntities > 0; j++ ) {
				if( map[i][j] == Tile.ROOM ) {
					if( generator.nextInt(10) == 1 ) {
						// Spawn entity
						EntityCardPic ecp = new EntityCardPic(texWall, entityModel, (i+0.5f)*scale, (j+0.5f)*scale, 5, 10, d, scale);
						entities.add( ecp );
						maxEntities --;
						//System.out.println("Entity spawned!");
					}
				}
			}
		}
		// **************************************************************** //
		
		
		playerLight = new PointLight(0.0f, 0.0f, 0.0f, 128.0f, 255.0f, 255.0f, 255.0f, 1.0f);
		lights.add(playerLight);
	}

	@Override
	public void update() {
		
		// Movement and mouse look
		mouseLook();
		movement();

		
		
		// Lights
		for( PointLight pl : lights) pl.update();
		
		final Camera cam = app3D.getCamera();
		playerLight.x = cam.getX();
		playerLight.y = cam.getY();
		playerLight.z = cam.getZ();
		
		// Entities
		for( Entity e : entities ) e.update();
		
		
		/*if( Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !placed) {
			PointLight pl = new PointLight(cam.getX(), cam.getY(), cam.getZ(), 64.0f, 220.0f, 180.0f, 25.0f, 1.0f);
			lights.add(pl);
			placed = true;
		}else
		if( !Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			placed = false;
		}*/
		
		
		
		// check for end game
		if( Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
	}

	@Override
	public void render3D() {
		// ****************** //
		// Shader properties
		final Camera cam = app3D.getCamera();
		float cx, cy, cz;
		cx = cam.getX();
		cy = cam.getY();
		cz = cam.getZ();
		Comparator<PointLight> c = new Comparator<PointLight>() {
			
			@Override
			public int compare(PointLight o1, PointLight o2) {
				
				float l1 = (float) Math.sqrt( (o1.x-cx)*(o1.x-cx) + (o1.y-cy)*(o1.y-cy) + (o1.z-cz)*(o1.z-cz));
				float l2 = (float) Math.sqrt( (o2.x-cx)*(o2.x-cx) + (o2.y-cy)*(o2.y-cy) + (o2.z-cz)*(o2.z-cz));
				return (l1 < l2)?-1:1;
			}
		};
		lights.sort(c);
		for( int i=0; i < Math.min(16, lights.size()); i++ ){

			PointLight e = lights.get(i);
			app3D.getActiveShader().setLight(i, e.x, e.y, e.z, e.range);
		}
		
		app3D.getActiveShader().setUniformLighting();
		// ********************* //
		// Temp render map
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		/*app3D.getWorldMatrix().setIdentity();
		app3D.matrixWorldBind();
		for( int i=0; i < mapSize; i++ ) {
			for( int j=0; j < mapSize; j++ ) {
				if( map[i][j] == Tile.Room ) {
					//app3D.getRenderUtils().drawQuad(i*scale, j*scale, -scale, -scale);
				}
			}
		}
		
		texWall.bind();
		for( int i=0; i < mapSize; i++ ) {
			for( int j=0; j < mapSize; j++ ) {
				if( map[i][j] == Tile.Wall ) {
					app3D.getWorldMatrix().setIdentity();
					app3D.getWorldMatrix().translate(new Vector3f(i*scale, j*scale, 0.0f));
					app3D.getWorldMatrix().scale(new Vector3f( scale, scale, scale));
					app3D.matrixWorldBind();
					block.render();
				}
			}
		}*/
		
		// Draw floors
		texDungeon.bind();
		app3D.getWorldMatrix().setIdentity();
		app3D.getWorldMatrix().scale(new Vector3f( scale, scale, scale));
		app3D.matrixWorldBind();
		sceneFloors.render();
		
		// Draw walls
		//texWall.bind();
		sceneWalls.render();
		
		// Draw details
		texDetail.bind();
		sceneDetails.render();
		
		// Entities
		for( Entity e : entities ) e.render();
		
	}

	@Override
	public void render2D() {

	}

	@Override
	public void destroy() {

	}
	
	
	
	////////////////
	// Mouse look
	private void mouseLook() {
		Camera cam = app3D.getCamera();
		
		float sensitivity = 750.0f;
		
		float pitch, yaw;
		pitch = cam.getPitch();
		yaw   = cam.getYaw();
		
		if (Display.isActive()) {
			int mx = Mouse.getX();
			int my = Mouse.getY();

			int cx = app3D.getWindowWidth() / 2;
			int cy = app3D.getWindowHeight() / 2;

			int dx = mx - cx;
			int dy = my - cy;
			
			
			pitch -= dy / sensitivity;
			yaw -= dx / sensitivity;

			// Reset Mouse
			pitch = (float) Math.min(Math.max(-Math.PI, pitch), 0);
			Mouse.setCursorPosition(cx, cy);
		}
		
		cam.setPitch( pitch );
		cam.setYaw( yaw );
	}
	
	private void movement(){
		Camera cam = app3D.getCamera();
		
		cam.wobble += 0.05;
		
		Vector3f position, direction;
		position  = cam.getPosition();
		direction = cam.getDirection();
		
		float yaw, speed;
		yaw   = cam.getYaw();
		speed = 1.0f;

		
		
		direction.set((float) Math.cos(yaw + Math.PI / 2.0) * speed, (float) Math.sin(yaw + Math.PI / 2.0) * speed, 0);
		
		boolean pressed = false;
		// Movement controls
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if( map_free(position.x - direction.x - playerPadding*sign(direction.x), position.y, position )){
				position.x -= direction.x;
			}
			if( map_free(position.x , position.y - direction.y - playerPadding*sign(direction.y), position )){
				position.y -= direction.y;
			}
			pressed = true;
			//Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			if( map_free(position.x + direction.x +  playerPadding*sign(direction.x), position.y, position )){
				position.x += direction.x;
			}
			if( map_free(position.x , position.y + direction.y + playerPadding*sign(direction.y), position )){
				position.y += direction.y;
			}
			pressed = true;
			//Vector3f.add(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			if( map_free(position.x - direction.x - playerPadding*sign(direction.x), position.y, position )){
				position.x -= direction.x;
			}
			if( map_free(position.x , position.y - direction.y - playerPadding*sign(direction.y), position )){
				position.y -= direction.y;
			}
			pressed = true;
			//direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			//Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			if( map_free(position.x + direction.x + playerPadding*sign(direction.x), position.y, position )){
				position.x += direction.x;
			}
			if( map_free(position.x , position.y + direction.y + playerPadding*sign(direction.y), position )){
				position.y += direction.y;
			}
			pressed = true;
			//direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			//Vector3f.add(position, direction, position);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			if(position.z == 16f)
			{
				vspeed = 1.4f;
			}
		}
		if (position.z + vspeed <= 16)
		{
			vspeed = 0;
			position.z = 16;
		}
		else
		{
			vspeed-=0.08f;
		}
		System.out.println(vspeed);
		position.z += vspeed;
		
		if( pressed ) {
			cam.wobble += 0.38*direction.length();
		}
		
		if( teleport ) {
			position = telepos;
			teleport = false;
		}
		
		cam.setPosition( position );
		cam.setDirection( direction );
	}
	
	// *************************************************************** //
	// Generate Scene from map
	private VertexBuffer generateScene_Floors( Tile[][] map ) {
		VertexBuffer vb = new VertexBuffer();
		
		// Generate Floors
		for( int i=1; i < mapSize-1; i++ ) {
			for( int j=01; j < mapSize-1; j++ ) {
				if( map[i][j] == Tile.ROOM || map[i][j] == Tile.CORRIDOR ) {
					//vb.addFloor(i, j, i+1.0f, j+1.0f, 0.0f);
					
					Matrix4f mf = new Matrix4f();
					mf.setIdentity();
					mf.translate(new Vector3f(i+1, j, 0.0f));
					vb.addModel(floorModel, mf);
					
					mf.setIdentity();
					mf.translate(new Vector3f(i+1, j, 1.5f));
					vb.addModel(ceilingModel, mf);
				}
				if( map[i][j] == Tile.CORRIDOR ) {
					// Determine cooridoor direction
					boolean corner = false;
					Tile up, down, left, right;
					up    = map[i][j-1];
					down  = map[i][j+1];
					left  = map[i-1][j];
					right = map[i+1][j];
					
					boolean rotate = false;
					if( (left == Tile.CORRIDOR || left == Tile.ROOM )|| (right == Tile.CORRIDOR || right == Tile.ROOM ) && !(Tile.CORRIDOR == up || Tile.ROOM == up || Tile.CORRIDOR == down || Tile.ROOM == down) ) {
						rotate = true;
					}
					if( (left  == Tile.CORRIDOR||left == Tile.ROOM) && (up   == Tile.CORRIDOR|| up == Tile.ROOM) || 
						(right == Tile.CORRIDOR||right == Tile.ROOM) && (up   == Tile.CORRIDOR|| up == Tile.ROOM) || 
						(left  == Tile.CORRIDOR||left == Tile.ROOM ) && (down == Tile.CORRIDOR|| up == Tile.ROOM) || 
						(right == Tile.CORRIDOR||right == Tile.ROOM) && (down == Tile.CORRIDOR|| up == Tile.ROOM) ){
						corner = true;
					}
					if( up == Tile.DOOR || down == Tile.DOOR || left == Tile.DOOR || right == Tile.DOOR) {
						corner = true;
					}
					if( !corner ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i, j, 0.0f));
						if( rotate ) {
							mf.rotate((float) Math.PI/2, new Vector3f( 0.0f, 0.0f, 1.0f));
							mf.translate(new Vector3f( 0.0f, -0.5f, 0.0f ));
						}else{
							mf.translate(new Vector3f( 0.0f, 0.5f, 0.0f ));
						}
						vb.addModel(archModel, mf );
					}
				}
			}
		}
		vb.prepareNormals();
		vb.freeze();
		return vb;
	}
	
	private VertexBuffer generateScene_Walls( Tile[][] map ) {
		VertexBuffer vb = new VertexBuffer();
		
		// Generate Floors
		for( int i=1; i < mapSize-1; i++ ) {
			for( int j=1; j < mapSize-1; j++ ) {

				// If is wall tile
				if (map[i][j] == Tile.WALL || map[i][j] == Tile.DOOR ) {
					// Get surrounding tiles
					Tile up, down, left, right;
					up    = map[i][j-1];
					down  = map[i][j+1];
					left  = map[i-1][j];
					right = map[i+1][j];
					boolean doorplaced = false;
					// UP
					if( up == Tile.ROOM || up == Tile.CORRIDOR ) {
						//vb.addWall(i, j, 0, i+1, j, 1);
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i, j, 0.0f));
						mf.rotate((float) Math.PI, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
						
						if( map[i][j] == Tile.DOOR && !doorplaced ) {
							vb.addModel(doorModel, mf);
							doorplaced = true;
						}
					}
					
					// DOWN
					if( down == Tile.ROOM || down == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i+1, j+1, 0.0f));
						//mf.rotate((float) Math.PI, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
						
						if( map[i][j] == Tile.DOOR && !doorplaced ) {
							vb.addModel(doorModel, mf);
							doorplaced = true;
						}
					}
					
					// LEFT
					if( left == Tile.ROOM || left == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i, j+1, 0.0f));
						mf.rotate((float) Math.PI/2, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
						
						if( map[i][j] == Tile.DOOR && !doorplaced ) {
							vb.addModel(doorModel, mf);
							doorplaced = true;
						}
					}
					
					// RIGHT
					if( right == Tile.ROOM || right == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i+1, j, 0.0f));
						mf.rotate((float) -Math.PI/2, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
						
						if( map[i][j] == Tile.DOOR && !doorplaced ) {
							vb.addModel(doorModel, mf);
							doorplaced = true;
						}
					}
				}
			}
		}
		vb.prepareNormals();
		vb.freeze();
		return vb;
	}
	
	public VertexBuffer generateScene_Details( Tile[][] map ) {
		VertexBuffer vb = new VertexBuffer();
		
		for( int i=1; i < mapSize-1; i++ ) {
			for( int j=1; j < mapSize-1; j++ ) {
				if( map[i][j] == Tile.ROOM ) {
					Tile upt, downt, leftt, rightt;
					upt    = map[i][j-1];
					downt  = map[i][j+1];
					leftt  = map[i-1][j];
					rightt = map[i+1][j];
					
					boolean up, down, left, right;
					up    = (upt == Tile.CORRIDOR || upt == Tile.ROOM );
					down  = (downt == Tile.CORRIDOR || downt == Tile.ROOM );
					left  = (leftt == Tile.CORRIDOR || leftt == Tile.ROOM );
					right = (rightt == Tile.CORRIDOR || rightt == Tile.ROOM );
					
					boolean corner = (up && left || up&& right || down && left || down && right) && !(down && up) && !(left&&right);
					if( corner ) {
						if ( generator.nextDouble()*100.0 <= 25.0 ){
							Matrix4f mf = new Matrix4f();
							mf.setIdentity();
							
							mf.translate(new Vector3f(i+0.5f, j+0.5f, 0.0f));
							mf.rotate((float) -Math.PI/4, new Vector3f( 0.0f, 0.0f, 1.0f));
							vb.addModel(candelabraModel, mf);
							mf.rotate((float) ((float) -Math.PI/1.2), new Vector3f( 0.0f, 0.0f, 1.0f));
							mf.translate(new Vector3f(0.2f, 0.2f, 0.0f));
							vb.addModel(candelabraModel, mf);
							
							PointLight pl = new PointLightFlickering((i+0.5f)*scale, (j+0.5f)*scale, 16.0f, 32.0f, 240.0f, 120.0f, 60.0f, 1.0f);
							lights.add(pl);
						}
					}
				}else if( map[i][j] == Tile.CORRIDOR ) {
					
					Tile upt, downt, leftt, rightt;
					upt    = map[i][j-1];
					downt  = map[i][j+1];
					leftt  = map[i-1][j];
					rightt = map[i+1][j];
					
					boolean[] a = new boolean[4];
					a[0] = (upt == Tile.WALL );
					a[1] = (downt == Tile.WALL );
					a[2] = (leftt == Tile.WALL );
					a[3] = (rightt == Tile.WALL );
					
					boolean[] b = new boolean[4];
					b[0] = (upt == Tile.ROOM );
					b[1] = (downt == Tile.ROOM );
					b[2] = (leftt == Tile.ROOM );
					b[3] = (rightt == Tile.ROOM );
					
					ArrayList<Boolean> al = new ArrayList<>();
					ArrayList<Integer> ai = new ArrayList<>();
					for( int x=0; x < 4; x++ ) {
						if( a[x]){
							al.add(a[x]);
							ai.add(x);
						}
						if( b[x]){
							al.clear();
							ai.clear();
							x = 4;
						}
					}
					
					int count = ai.size();
					if( count > 0 ) {
						if ( generator.nextDouble()*100.0 <= 45.0 ){
							Matrix4f mf = new Matrix4f();
							mf.setIdentity();
							mf.translate(new Vector3f(i, j+1, 0.5f));
							int lp = 0;
							if( count > 1 ) {
								lp = generator.nextInt(count-1);
							} else {
								lp = 0;
							}
							switch( ai.get(lp) ) {
							
								// up:
								case 0:
									//mf.rotate((float) ((float) Math.PI), new Vector3f( 0.0f, 0.0f, 1.0f));
								break;
								
								// down
								case 1:
									mf.rotate((float) ((float) -Math.PI), new Vector3f( 0.0f, 0.0f, 1.0f));
									mf.translate(new Vector3f(0.0f, 0.0f, 0.0f));
								break;
								
								// left:
								case 2:
									mf.rotate((float) ((float) -Math.PI/2), new Vector3f( 0.0f, 0.0f, 1.0f));
									mf.translate(new Vector3f(0.0f, 1.0f, 0.0f));
								break;
								
								// right:
								case 3:
									mf.rotate((float) ((float) Math.PI/2), new Vector3f( 0.0f, 0.0f, 1.0f));
									
								break;
							}
							vb.addModel(torch1Model, mf);
							PointLight pl = new PointLightFlickering((i)*scale, (j+1)*scale, 16.0f, 32.0f, 240.0f, 120.0f, 60.0f, 1.0f);
							lights.add(pl);
						}
					}
				}
			}
		}
		
		vb.prepareNormals();
		vb.freeze();
		return vb;
	}
	
	public boolean map_free( float x, float y, Vector3f pos ) {
		if( Keyboard.isKeyDown(Keyboard.KEY_C)){
			return true;
		}
		int gX, gY;
		gX = (int)Math.floor(x/scale);
		gY = (int)Math.floor(y/scale);
		
		if( gX >= 0 && gX < mapSize ) {
			if( gY >= 0 && gY <= mapSize ) {
				Tile a = map[gX][gY];
				if( a == Tile.DOOR ) {
					Camera cam = app3D.getCamera();
					
					int room = generator.nextInt(d.rooms.length-1);
					telepos =(new Vector3f((float)d.rooms[room].getCenterX()*scale,
							(float)d.rooms[room].getCenterY()*scale, 16.0f ));
					teleport = true;
					return true;
				}
				if( a == Tile.ROOM || a == Tile.EMPTY || a == Tile.CORRIDOR ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public float sign( float x){
		return (x>=0)?1:-1;
	}

}
