package Application3D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.newdawn.slick.openal.Audio;

import utils3D.Texture;
import utils3D.VertexBuffer;
import static org.lwjgl.opengl.GL13.*;

public class Resources {
	/** The resources class contains a reference to
	 * all loaded openGL resources such as vertex buffers,
	 * textures, sounds etc.
	 * This is to make sure everything gets unloaded when the game ends.
	 * 
	 * It also provides a uniform means of accessing resources from multiple places via the use of 
	 * Hashmap keys.
	 * 
	 * This was chosen as an extra safety mechanism to prevent two things:
	 * 1) Floating resources: When resources get left in memory and there if no feasible way to collect them. Whilst java resources can be unloaded through GC, 
	 *                        openGL/openAL allocations Cannot. Therefore this class exists as a registry for all resources, to ensure everything that is being used, is
	 *                        tracked.
	 *   
	 * 2) Resource duplication/time-independence: Avoid duplication of loading already existing resources. If two objects need to make use of the same resource, they can
	 *                                            request the resource from the resource system, rather than having to reload it themselves.
	 */
	private HashMap<String, Texture> textures;
	private HashMap<String, VertexBuffer> vertexBuffers;
	private HashMap<String, Audio> sounds;
	
	public Resources(){
		textures 		= new HashMap<>();
		vertexBuffers 	= new HashMap<>();
		sounds          = new HashMap<>();
	}
	
	public Texture loadTexture( String filename, String key ){
	    Texture tex = this.getTexture( key );
	    if( this.getTexture( key ) == null ){
	        tex = new Texture( filename, GL_TEXTURE0 );
	        this.addTexture( tex, key );
	    }
	    return tex;
	}
	
	public Texture loadTexture( BufferedImage image, String key ){
        Texture tex = this.getTexture( key );
        if( this.getTexture( key ) == null ){
            tex = new Texture( image, GL_TEXTURE0 );
            this.addTexture( tex, key );
        }
        return tex;
    }
	
	public void unloadTexture( String key ){
	    Texture tex = this.getTexture( key );
	    if( this.getTexture( key ) != null ){
	        tex.destroy();
	        this.textures.remove( key );
	    }
	}
	
	private void addTexture( Texture t, String key ){
	    textures.put(  key, t );
	}
	public void addModel( VertexBuffer v, String key ) {
	    vertexBuffers.put(  key, v );
	}
	public void addSound( Audio a, String key ){
	    sounds.put( key, a );
	}
	
	public Texture getTexture( String key ){
	    return textures.get(  key );
	}
	public VertexBuffer getVertexBuffer( String key ){
        return vertexBuffers.get(  key );
    }
    public Audio getSound( String key ){
        return sounds.get(  key );
    }
	
	public void destroy(){
		for( Texture t : textures.values() ) t.destroy();
		for( VertexBuffer v : vertexBuffers.values() ) v.destroy();
	}
	
	

}
