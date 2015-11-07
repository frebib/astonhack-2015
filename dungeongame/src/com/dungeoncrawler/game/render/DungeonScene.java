package com.dungeoncrawler.game.render;

import Application3D.Application3D;
import Application3D.Renderer3D;
import utils3D.Camera;
import utils3D.RenderUtils;
import utils3D.Texture;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class DungeonScene implements Renderer3D {

	private Application3D app3D = Application3D.getApp();
	private Texture tex0;
	
	@Override
	public void init() {
		// Init
		System.out.println("Creating Dungeon Renderer");
		
		// Set initial position
		app3D.getCamera().setPosition(new Vector3f( 256.0f, 256.0f, 12.0f ));
		
		// Load in some resources
		tex0 = app3D.getResources().loadTexture("res/textures/brick.png", "texBrick");
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
		tex0.bind();
		app3D.getRenderUtils().drawQuad(0, 0, 1024, 1024);
	}

	@Override
	public void render2D() {
		app3D.getRenderUtils().drawQuad(0, 0, 100, 100);
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
		speed = 6.0f;
		
		direction.set((float) Math.cos(yaw + Math.PI / 2.0) * speed, (float) Math.sin(yaw + Math.PI / 2.0) * speed, 0);
		
		
		// Movement controls
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			Vector3f.add(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			Vector3f.sub(position, direction, position);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			direction.set((float) Math.cos(yaw) * speed, (float) Math.sin(yaw) * speed, 0);
			Vector3f.add(position, direction, position);
		}
		
		cam.setPosition( position );
		cam.setDirection( direction );
	}

}
