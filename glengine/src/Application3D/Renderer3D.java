package Application3D;

public interface Renderer3D {
    /** This interface is designed to provide a means through which instances that must utilize the openGL pipeline are interacted with 
     * in the correct manner.
     * All OpenGL initialisation must happen within the init method, rather than in the class constructor. 
     * This is to ensure that no openGL resources are used before they have been initialised. It also ensures the initialisation occurs
     * in such a place that is both thread safe and in-keeping with the integrity of the rendering pipeline defined by SCApplication3D.
     * 
     * update, render3D, render2D and  are all called continuously inside the main rendering loop in that order. Each has the following purpose:
     * 
     * - update: Perform any view-related (View in MVC) updates e.g: tweens, transitions, fades, hover/click event checking, etc;
     *           Updates occur first, and the idea is to put updates in here, incase an objects rendering is temporarily disabled, but
     *           updates should continue.
     *           This also ensures the state of all objects has been updated before they are rendered to the screen. (This should potentially be able to run
     *           on a separate game-control thread if a game setup had two (or more) real-time loops).
     *           
     * - render3D: This performs all 3D rendering. By default SCApplication3D will assign the default rendering shader, bind the identity matrix to the world
     *             uniform and update the view and projection matrices to the camera's 3D rendering modes. Any change to a rendering shader will require a
     *             resubmission of the world matrix and camera matrices to the GPU.
     *             This method is designed to contain only the actual rendering and transformation of 3d objects. Updating should not occur in here.
     *             
     * - render2D: this method performs 2D rendering. The SCApplication3D will bind the 2D rendering matrix (which cuts out some functionality such as lighting). It also
     *             sets up an orthographic projection (i.e the projection maintains size over distance, non-perspective rendering;). All 2D HUD rendering should be 
     *             performed here
     *             
     *  The final method is destroy. This is automatically called by the instances Renderer3D is registered to upon game end, or a change of state in the renderer.
     *  
     *  == Composite Design pattern ==
     *  
     *  The Renderer3D interface is designed around the composite pattern, with the idea that all renderer3Ds are registered to either the SCApplication3D or another
     *  renderer3D. This allows the main application to maintain control of the overall pipeline, whilst being able to keep a hierachical object design in such a way
     *  that if a base object (for example a scene) which consists of more independent rendering instances needs to be destroyed, it's registererd instances can also
     *  be unloaded correctly at no extra work on the registrants part.
     */
    
	// Init function called inside the scope of the GL thread
	public void init();
	
	// Update
	public void update();
	
	// Render 3D function as a 3D render pass.
	public void render3D();
	
	// Render 2D function as an orthographic 2D render pass.
	public void render2D();
	
	// A destroy function which unloads all associated GL
	public void destroy();
}
