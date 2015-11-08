package Application3D;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;

import utils3D.AudioUtils;
import utils3D.Camera;
import utils3D.ModelImporter;
import utils3D.RenderUtils;
import utils3D.Shader;
import utils3D.Texture;
import utils3D.VertexBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic; 

public class Application3D extends Thread {
	/** The Application3D class runs entirely in its own thread.
	 * - It uses the composition model to control all rendering
	 * of instances. Anything that desires to render must implement 'Renderer3D'.
	 * 
	 * Any instance will be initialised inside the scope of the thread, however it can be
	 * registered from anywhere.
	 * 
	 * The class is designed as a singleton, so that it can be accessed by all game objects.
	 */
    // Singleton
    private static Application3D instance = new Application3D();
	// Window properties
	private int windowWidth 	= 1280;
	private int windowHeight 	= 720;
	private String windowTitle	= "WikiDungeon";
	
	// Instances
	private ArrayList<Renderer3D> newRenderInstances;
	private ArrayList<Renderer3D> renderInstances;
	private Set<FactoryInstance3D> destructibleInstances;
	
	// Camera and shaders
	private Camera camera;
	private Shader shader;
	private Shader shader2D;
	private Matrix4f matrixWorld;
	private FloatBuffer bufferedMatrixWorld;
	private RenderUtils  renderUtils;
	private AudioUtils   audioUtils;
	private Shader activeShader = null;
	
	// Room controllers
	private Renderer3D roomMenu, roomGame, roomGameSetup;
	private Renderer3D activeRoom;
	
	// Ready
	private boolean ready = false;
	
	// Create somewhere to store resources
	// - It is defined as being the resposibility of the resources
	//   class to unload all resources, not the responsibility
	//   of the instances that use those resources.
	private Resources resources = new Resources();
	
	// Constructor
	public Application3D(){
		newRenderInstances 	= new ArrayList<>();
		renderInstances		= new ArrayList<>();
		destructibleInstances = new HashSet<>();
		matrixWorld			= new Matrix4f();
		matrixWorld.setIdentity();
		bufferedMatrixWorld = BufferUtils.createFloatBuffer(16);
		ready = false;
	}
	
	public void run(){
		// Create GL context
		initOpenGL();
		
		camera 	 = new Camera( 80.0f, windowWidth, windowHeight, this );
		shader 	 = new Shader( "shaders/sy_vertex.glsl", "shaders/sy_frag.glsl");
		shader2D = new Shader( "shaders/sy_vertex2D.glsl", "shaders/sy_frag2D.glsl");
		
	    // Prepare model utils class
        renderUtils = new RenderUtils();
        audioUtils  = new AudioUtils();
        
        // ROOMS
        
        // Set ready
        ready = true;
        
        // Set game as main room [temp, this will be changed to menu]
        setActiveRoom( roomMenu );
        
		// GL Loop
		while (!Display.isCloseRequested()) {
			// Check for resize
		    if ( Display.getWidth() != windowWidth || Display.getHeight() != windowHeight ) {
		        windowWidth  = Display.getWidth();
		        windowHeight = Display.getHeight();
		        System.out.println("GLAPP::Window resized to: "+windowWidth+" "+windowHeight);
		        
		        // Update viewport and camera dimensions
		        glViewport( 0, 0, windowWidth, windowHeight );
		        camera.setDimensions(  windowWidth, windowHeight );
		    }
		    
			this.initInstances();
			
			// Render instances
			glClearColor( 0.6f, 0.7f, 1.0f, 1.0f );
			glClearDepth( 1.0f );
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.update();
			glEnable(GL_DEPTH_TEST); // Enable depth testing for 3D
			this.render3D();
			glDisable(GL_DEPTH_TEST); // Disable when rendering in 2D
			this.render2D();
			
			this.removeDestructibles();
			
			// Sync display
			Display.sync( 30 );
			Display.update();
		}
		
		// Destroy on finish
		this.destroy();
	}
	
	// INITIALISE OPENGL
	private void initOpenGL(){
		// Setup display and context through LWJGL
		try {
		    PixelFormat pixelFormat = new PixelFormat(24, 0, 0, 0, 0);
		    ContextAttribs contextAttributes = new ContextAttribs(3, 0).withForwardCompatible(true);
		    
			Display.create( pixelFormat, contextAttributes );
			Display.setTitle( windowTitle );
			Display.setDisplayMode( new DisplayMode(windowWidth, windowHeight));
		} catch ( LWJGLException e ) {
			e.printStackTrace();
			System.exit( -1 );
		}
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		
		// Initialise screen properties:
		glClearColor( 0.6f, 0.7f, 1.0f, 1.0f );
		glViewport( 0, 0, windowWidth, windowHeight );
		glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8);
		glEnable( GL_BLEND ); 
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
		Display.setResizable( true );

		
		// Load universal resources
		loadUniversalResources();
	}
	
	// Load universal resources
	private void loadUniversalResources(){
	    
	    // Load popup graphics
	   /* Texture sprPopupDrop           = resources.loadTexture( "res/GUI/sprPopupDrop.png", "sprPopupDrop" );
	    Texture sprPopupDrop2          = resources.loadTexture( "res/GUI/sprPopupDrop2.png", "sprPopupDrop2" );
	    Texture sprPopupDrop3          = resources.loadTexture( "res/GUI/sprPopupDrop3.png", "sprPopupDrop3" );
	    Texture sprPopupDrop4          = resources.loadTexture( "res/GUI/sprPopupDrop4.png", "sprPopupDrop4" );
	    Texture sprTicketSmall_Taxi    = resources.loadTexture( "res/GUI/sprTicketSmall_Taxi.png", "sprTicketSmall_Taxi" );
	    Texture sprTicketSmall_Bus     = resources.loadTexture( "res/GUI/sprTicketSmall_Bus.png", "sprTicketSmall_Bus" );
	    Texture sprTicketSmall_Train   = resources.loadTexture( "res/GUI/sprTicketSmall_Train.png", "sprTicketSmall_Train" );
	    Texture sprTicketSmall_Black   = resources.loadTexture( "res/GUI/sprTicketSmall_Black.png", "sprTicketSmall_Black" );
	    Texture sprYouAreHere          = resources.loadTexture( "res/GUI/sprYouAreHere.png", "sprYouAreHere" );
	    
	    Texture sprTicketSmall_Black_LeftHalf  = resources.loadTexture( "res/GUI/sprTicketSmall_Black_LeftHalf.png", "sprTicketSmall_Black_LeftHalf");
	    Texture sprTicketSmall_Black_RightHalf = resources.loadTexture( "res/GUI/sprTicketSmall_Black_RightHalf.png", "sprTicketSmall_Black_RightHalf");
	    Texture sprTicketSmall_Bus_LeftHalf    = resources.loadTexture( "res/GUI/sprTicketSmall_Bus_LeftHalf.png", "sprTicketSmall_Bus_LeftHalf");
	    Texture sprTicketSmall_Bus_RightHalf   = resources.loadTexture( "res/GUI/sprTicketSmall_Bus_RightHalf.png", "sprTicketSmall_Bus_RightHalf");
	    Texture sprTicketSmall_Taxi_LeftHalf   = resources.loadTexture( "res/GUI/sprTicketSmall_Taxi_LeftHalf.png", "sprTicketSmall_Taxi_LeftHalf");
	    Texture sprTicketSmall_Taxi_RightHalf  = resources.loadTexture( "res/GUI/sprTicketSmall_Taxi_RightHalf.png", "sprTicketSmall_Taxi_RightHalf");
	    Texture sprTicketSmall_Train_LeftHalf  = resources.loadTexture( "res/GUI/sprTicketSmall_Train_LeftHalf.png", "sprTicketSmall_Train_LeftHalf");
	    Texture sprTicketSmall_Train_RightHalf = resources.loadTexture( "res/GUI/sprTicketSmall_Train_RightHalf.png", "sprTicketSmall_Train_RightHalf");
	    
	    Texture texGamePiece_Black = resources.loadTexture( "res/textures/texGamePiece_Black.png", "texGamePiece_Black" );
	    Texture texGamePiece_Blue = resources.loadTexture( "res/textures/texGamePiece_Blue.png", "texGamePiece_Blue" );
	    Texture texGamePiece_Green = resources.loadTexture( "res/textures/texGamePiece_Green.png", "texGamePiece_Green" );
	    Texture texGamePiece_Red = resources.loadTexture( "res/textures/texGamePiece_Red.png", "texGamePiece_Red" );
	    Texture texGamePiece_Yellow = resources.loadTexture( "res/textures/texGamePiece_Yellow.png", "texGamePiece_Yellow" );
	    Texture texGamePiece_White = resources.loadTexture( "res/textures/texGamePiece_White.png", "texGamePiece_White" );
	    
	    Texture sprFlagNormal      = resources.loadTexture( "res/GUI/sprFlagNormal.png", "sprFlagNormal" );
	    Texture sprFlagTaxi        = resources.loadTexture( "res/GUI/sprFlagTaxi.png", "sprFlagTaxi" );
	    Texture sprFlagBus         = resources.loadTexture( "res/GUI/sprFlagBus.png", "sprFlagBus" );
	    Texture sprFlagUnderground = resources.loadTexture( "res/GUI/sprFlagUnderground.png", "sprFlagUnderground" );
	    Texture sprFlagSecret      = resources.loadTexture( "res/GUI/sprFlagSecret.png", "sprFlagSecret" );
	    
	    VertexBuffer modPlayerPiece = ModelImporter.importModel( "res/models/modelGamePiece.gmmod" );
	    modPlayerPiece.freeze();
	    resources.addModel( modPlayerPiece, "modPlayerPiece" );*/
	}
	
/////////////////////////////////////////////////////////////////
	// INIT 
	synchronized private void initInstances(){
    	// Initialise new renderer instances
        for( Renderer3D r : newRenderInstances ) {
            r.init();
            renderInstances.add( r );
        }
        newRenderInstances.clear();
	}
	
	// UPDATE
	synchronized private void update(){
		Keyboard.poll();
		camera.update();
		for( Renderer3D r : renderInstances ) r.update();
	}
	// RENDER 3D
	synchronized private void render2D(){
		
	    this.setActiveShader( shader2D );
		{
			camera.setProjection2D(shader2D);
			shader2D.setUniformColour( 1, 1, 1, 1 );
			for ( Renderer3D r : renderInstances ) r.render2D();
		}
		this.resetShader();
	}
	
	// RENDER 2D
	synchronized private void render3D(){
		
		this.setActiveShader( shader );
		{
		 
			camera.setProjection3D(shader);
			this.matrixWorldBind();
			shader.setUniformColour( 1, 1, 1, 1 );
			//this.activeRoom.render3D();
			for ( Renderer3D r : renderInstances ) r.render3D();
		}
		this.resetShader();
	}
	
	// REMOVE DESTRUCTIBLES
	synchronized private void removeDestructibles(){
	    for( FactoryInstance3D fi : destructibleInstances ) {
	        unregisterRenderInstance( fi );
	    }
	    destructibleInstances.clear();
	}
	
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Registers an instance to the internal rendering system
	 * @param r
	 */
	public void registerRenderInstance( Renderer3D r ) {
		newRenderInstances.add( r );
	}
	
	/**
	 * Unregisters and unloads an instance from the internal system
	 * @param r
	 */
	public void unregisterRenderInstance( Renderer3D r ) {
		newRenderInstances.remove( r );
		if( renderInstances.contains( r )){ // Unload if in list of initialised instances
		    r.destroy();
		}
		renderInstances.remove( r );
		
	}
	
	/**
	 * Sets an instance within the system ready for destruction
	 * @param fi
	 */
	public void registerDestructibleInstance( FactoryInstance3D fi ){
	    this.destructibleInstances.add( fi );
	}
	
	public Matrix4f getWorldMatrix(){
		return matrixWorld;
	}
	
	public void matrixWorldBind(){
		matrixWorld.store( bufferedMatrixWorld );
		bufferedMatrixWorld.flip();
		
		glUniformMatrix4( this.getActiveShader().getUniformLocationMatrixWorld(), false, bufferedMatrixWorld );
		matrixWorld.invert();
		matrixWorld.store( bufferedMatrixWorld );
        bufferedMatrixWorld.flip();
		glUniformMatrix4( this.getActiveShader().getUniformLocationMatrixWorldInverse(), false, bufferedMatrixWorld );
	}
	
	/**
	 * @return A reference to the resources instance
	 */
	public Resources getResources(){
		return resources;
	}
	
	/**
	 * Unloads openGL Fully. This also iterates through all of the other components and ensures everything is unloaded.
	 * Once everything is unloaded, the openGL and openAL contexts will also be destroyed.
	 */
	public void destroy(){
		for ( Renderer3D r : newRenderInstances ) r.destroy();
		for ( Renderer3D r : renderInstances ) r.destroy();
		shader.destroy();
		resources.destroy();
		Display.destroy();
		AL.destroy();
		System.exit(0);
	}
	
	// [TEMP]
	public synchronized static void APP_CheckGLError( String state ){
		int err_id = glGetError();
		if ( err_id != GL_NO_ERROR ) {
			String error = GLU.gluErrorString(err_id);
			System.err.println( "GLAPP::ERROR -> "+error + " in "+state);
			Thread.dumpStack();
			System.err.println(" < Exiting >");
			System.exit(1);
		}
	}
	
	/**
	 * Prints out the current OpenGL Error stack and then exits the application if an error exists.
	 * This will also print the current execution stack.
	 * @param state
	 */
	public synchronized void appCheckGLError( String state ){
		int err_id = glGetError();
		if ( err_id != GL_NO_ERROR ) {
			String error = GLU.gluErrorString(err_id);
			System.err.println( "GLAPP::ERROR -> "+error + " in "+state);
			Thread.dumpStack();
			System.err.println(" < Exiting >");
			this.destroy();
			System.exit(1);
		}
	}
	
	public synchronized void setActiveRoom( Renderer3D activeRoom ){
	    this.activeRoom = activeRoom;
	}
	
	public synchronized Renderer3D getActiveRoom(){
	    return this.activeRoom;
	}
	
	// Getters
	/**
	 * @return The width in pixels of the window
	 */
	public synchronized int getWindowWidth(){
		return this.windowWidth;
	}
	/**
	 * @return Returns the height in pixels of the window
	 */
	public synchronized int getWindowHeight(){
		return this.windowHeight;
	}
	/**
	 * @return A reference to the model utils class
	 */
	public synchronized RenderUtils getRenderUtils(){
	    return this.renderUtils;
	}
	
	/**
	 * Returns a reference to the audio utils instance
	 * @return
	 */
	public synchronized AudioUtils getAudioUtils(){
	    return this.audioUtils;
	}
	
	/**
	 * Sets the active shader for rendering
	 * @param s
	 */
	public synchronized void setActiveShader( Shader s ){
	    if ( this.getActiveShader() != null ) {
	        resetShader();
	    }
	    this.activeShader = s;
	    this.activeShader.bind();
	}
	
	/**
	 * Restores the rendering shader to the default shader.
	 */
	public synchronized void setDefaultShader(){
	    setActiveShader( this.shader );
	}
	
	/**
	 * @return Returns the active shader reference
	 */
	public synchronized Shader getActiveShader(){
	    return this.activeShader;
	}
	
	/**
	 * Disables the current rendering shader. (i.e prevents any further rendering)
	 */
	public synchronized void resetShader(){
	    Shader s = getActiveShader();
	    if ( s != null ){
	        s.unbind();
	    }
	    this.activeShader = null;
	}
	
	/**
	 * Returns the active Camera
	 * @return
	 */
	public synchronized Camera getCamera(){
	    return this.camera;
	}
	
	//////////////////////////////////////////
	// FACTORY METHODS
	/* ---------------
	 * These factory methods are used to generate self-destructible objects where the only reference
	 * held will be in SCApplication3D's internal instance register.
	 * These will be used for generic popups / temporary rendering instances that are needed.
	 * 
	 */

	
	public synchronized void destructAll( Class<?> cType ){
    	for( Renderer3D c : this.renderInstances ){ if ( c instanceof FactoryInstance3D && cType.isInstance( c ) )  ( (FactoryInstance3D) c ).deleteBegin(); }
    	for( Renderer3D c : this.newRenderInstances ){ if ( c instanceof FactoryInstance3D &&cType.isInstance(  c )) ( (FactoryInstance3D) c ).deleteBegin(); }       
	}
	//////////////////////////////////////////
	/**
	 * Returns the singleton instance which can be used to interface with the 3D app, without having to hold
	 * a reference.
	 * @return
	 */
	public static Application3D getApp(){
	    return instance;
	}
	
	public synchronized boolean isReady(){
	    return this.ready;
	}
}
