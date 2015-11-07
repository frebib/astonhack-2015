package com.dungeoncrawler.game.render;

import Application3D.Application3D;
import Application3D.Renderer3D;
import utils3D.Camera;
import utils3D.ModelImporter;
import utils3D.RenderUtils;
import utils3D.Texture;
import utils3D.VertexBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.dungeoncrawler.generator.Tile;

public class DungeonScene implements Renderer3D {

	private Application3D app3D = Application3D.getApp();
	private Texture texFloor, texWall, texDungeon;
	private VertexBuffer sceneFloors, sceneWalls, archModel, wallModel, floorModel, ceilingModel;
	
	private Tile[][] map;
	private int mapSize = 30;
	private int scale   = 32; // Size of generated geometry per cell in units.
	private float playerPadding = 10.0f;
	
	
	@Override
	public void init() {
		// Init
		System.out.println("Creating Dungeon Renderer");
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
		
		// Setup rendering properties
		/*GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);*/
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8);
		
		// Set initial position
		app3D.getCamera().setPosition(new Vector3f( 400.0f, 400.0f, 16.0f ));
		
		// *********************************************** //
		// Create map
		map = new Tile[mapSize][mapSize];
		for( int i=0; i < mapSize; i++ ) {
			for( int j=0; j < mapSize; j++ ) {
				map[i][j] = Tile.EMPTY;
			}
		}
		for( int i=10; i < 20; i++ ) {
			for( int j=10; j < 15; j++ ) {
				map[i][j] = Tile.ROOM;
			}
		}
		
		for( int i=20; i < 25; i++ ) {
			for( int j=12; j < 13; j++ ) {
				map[i][j] = Tile.CORRIDOR;
			}
		}
		map[24][10] = Tile.CORRIDOR;
		map[24][11] = Tile.CORRIDOR;
		map[24][13] = Tile.CORRIDOR;
		map[24][14] = Tile.CORRIDOR;
		// place walls in
		for( int i=0; i < mapSize; i++ ) {
			for( int j=0; j < mapSize; j++ ) {
				
				
				if( i==0 || j == 0 || i == mapSize-1 || j == mapSize-1 ) {
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
		
		// Load in some resources
		texFloor = app3D.getResources().loadTexture("res/textures/brick.png", "texFloor");
		texWall  = app3D.getResources().loadTexture("res/textures/wall.png", "texWall");
		texDungeon = app3D.getResources().loadTexture("res/textures/dungeon.png", "texDungeon");
		
		archModel    = ModelImporter.importModel("res/models/Arch.gmmod");
		wallModel    = ModelImporter.importModel("res/models/Wall.gmmod");
		floorModel   = ModelImporter.importModel("res/models/Floor.gmmod");
		ceilingModel = ModelImporter.importModel("res/models/Ceiling.gmmod");
	
		// Generate block:
		//block = ModelImporter.importModel("res/models/block.gmmod");
		sceneFloors = generateScene_Floors( map );
		sceneWalls  = generateScene_Walls( map );
	
	}

	@Override
	public void update() {
		
		// Movement and mouse look
		mouseLook();
		movement();
		
		
		
		// check for end game
		if( Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			System.exit(0);
		}
	}

	@Override
	public void render3D() {
		// ****************** //
		// Shader properties
		Camera cam = app3D.getCamera();
		for( int i=0; i < 8; i++ ){
			app3D.getActiveShader().setLight(i, cam.getX(), cam.getY(), cam.getZ(), 256.0f);
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
		
		Vector3f position, direction;
		position  = cam.getPosition();
		direction = cam.getDirection();
		
		float yaw, speed;
		yaw   = cam.getYaw();
		speed = 2.0f;
		
		direction.set((float) Math.cos(yaw + Math.PI / 2.0) * speed, (float) Math.sin(yaw + Math.PI / 2.0) * speed, 0);
		
		
		// Movement controls
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if( map_free(position.x - direction.x - playerPadding*sign(direction.x), position.y )){
				position.x -= direction.x;
			}
			if( map_free(position.x , position.y - direction.y - playerPadding*sign(direction.y) )){
				position.y -= direction.y;
			}
			//Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			if( map_free(position.x + direction.x +  playerPadding*sign(direction.x), position.y )){
				position.x += direction.x;
			}
			if( map_free(position.x , position.y + direction.y + playerPadding*sign(direction.y) )){
				position.y += direction.y;
			}
			//Vector3f.add(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			if( map_free(position.x - direction.x - playerPadding*sign(direction.x), position.y )){
				position.x -= direction.x;
			}
			if( map_free(position.x , position.y - direction.y - playerPadding*sign(direction.y) )){
				position.y -= direction.y;
			}
			
			//direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			//Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			if( map_free(position.x + direction.x + playerPadding*sign(direction.x), position.y )){
				position.x += direction.x;
			}
			if( map_free(position.x , position.y + direction.y + playerPadding*sign(direction.y) )){
				position.y += direction.y;
			}
			//direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			//Vector3f.add(position, direction, position);
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
					if( left == Tile.CORRIDOR || right == Tile.CORRIDOR && !(Tile.CORRIDOR == up || Tile.CORRIDOR == down) ) {
						rotate = true;
					}
					if( left  == Tile.CORRIDOR && up   == Tile.CORRIDOR || 
						right == Tile.CORRIDOR && up   == Tile.CORRIDOR || 
						left  == Tile.CORRIDOR && down == Tile.CORRIDOR || 
						right == Tile.CORRIDOR && down == Tile.CORRIDOR ){
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
				if (map[i][j] == Tile.WALL ) {
					// Get surrounding tiles
					Tile up, down, left, right;
					up    = map[i][j-1];
					down  = map[i][j+1];
					left  = map[i-1][j];
					right = map[i+1][j];
					
					// UP
					if( up == Tile.ROOM || up == Tile.CORRIDOR ) {
						//vb.addWall(i, j, 0, i+1, j, 1);
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i, j, 0.0f));
						mf.rotate((float) Math.PI, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
					}
					
					// DOWN
					if( down == Tile.ROOM || down == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i+1, j+1, 0.0f));
						//mf.rotate((float) Math.PI, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
					}
					
					// LEFT
					if( left == Tile.ROOM || left == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i, j+1, 0.0f));
						mf.rotate((float) Math.PI/2, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
					}
					
					// RIGHT
					if( right == Tile.ROOM || right == Tile.CORRIDOR ) {
						Matrix4f mf = new Matrix4f();
						mf.setIdentity();
						
						mf.translate(new Vector3f(i+1, j, 0.0f));
						mf.rotate((float) -Math.PI/2, new Vector3f( 0.0f, 0.0f, 1.0f));
						vb.addModel(wallModel, mf);
						
						
					}
				}
			}
		}
		vb.prepareNormals();
		vb.freeze();
		return vb;
	}
	
	public boolean map_free( float x, float y ) {
		int gX, gY;
		gX = (int)Math.floor(x/scale);
		gY = (int)Math.floor(y/scale);
		
		if( gX >= 0 && gX < mapSize ) {
			if( gY >= 0 && gY <= mapSize ) {
				Tile a = map[gX][gY];
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
