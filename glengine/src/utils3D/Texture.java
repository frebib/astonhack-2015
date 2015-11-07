package utils3D;

import Application3D.Application3D;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {
	
	private int textureIndex = -1;
	private boolean loaded = false;
	private int width = 0;
	private int height = 0;
	
	/**
	 * Create an openGL texture from file
	 * @param filename
	 * @param textureUnit
	 */
	public Texture( String filename, int textureUnit ) {
		textureIndex 	= this.loadPNGTexture( filename, textureUnit );
		this.loaded 	= true;
		Application3D.getApp().appCheckGLError( "TEXTURE LOADING END");
	}
	
	/**
	 * Create an openGL texture from a buffered image
	 * @param image
	 * @param textureUnit
	 */
	public Texture( BufferedImage image, int textureUnit ){
	    
	    // Generate temporary pixel array to store data
	    int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        // Create byte buffer
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                int alphaVal = ((pixel >> 24) & 0xFF );
                if ( alphaVal < 12 ) alphaVal = 0;
                buffer.put((byte)alphaVal);
            }
        }
        buffer.flip();
        System.out.println(image.getWidth() + " "+ image.getHeight());
	    textureIndex = this.buildTexture( textureUnit, image.getWidth(), image.getHeight(), buffer );
	    System.out.println( "<font>" );
	    Application3D.getApp().appCheckGLError("TEXTURE LOAD FROM BUFFERED IMAGE END");
	    this.loaded    = true;
	}

	/**
	 * [This method is not written by me: Source: http://wiki.lwjgl.org/index.php?title=The_Quad_textured]
	 * Generates an openGL texture from a PNG file.
	 * @param filename
	 * @param textureUnit
	 * @return
	 */
	private int loadPNGTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;
        int tIndex = 0;
         
        try {
            // Open the PNG file as an InputStream
            InputStream in = new FileInputStream(filename);
            // Link the PNG decoder to this stream
            PNGDecoder decoder = new PNGDecoder(in);
       
            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();
             
            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect( 4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
            
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        tIndex = buildTexture( textureUnit, tWidth, tHeight, buf );
        
        return tIndex;
    }
	
	/**
	 * [This method is not written by me: Source: http://wiki.lwjgl.org/index.php?title=The_Quad_textured]
	 * Builds an openGL texture from a byte buffer and links it to a texture unit.
	 * @param textureUnit
	 * @param tWidth
	 * @param tHeight
	 * @param buf
	 * @return
	 */
	private int buildTexture(int textureUnit, int tWidth, int tHeight, ByteBuffer buf){
        // Create a new texture object in memory and bind it
        int texId = glGenTextures();
        Application3D.getApp().appCheckGLError( "TEXTURE GENTEXTURE");
        glActiveTexture(textureUnit);
        Application3D.getApp().appCheckGLError( "TEXTURE ACTIVETEXTURE");
        glBindTexture( GL_TEXTURE_2D, texId);
        Application3D.getApp().appCheckGLError( "TEXTURE BINDTEXTURE");
         
        // All RGB bytes are aligned to each other and each component is 1 byte
        glPixelStorei( GL_UNPACK_ALIGNMENT, 1);
         
        // Upload the texture data and generate mip maps (for scaling)
        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGBA, tWidth, tHeight, 0, 
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        Application3D.getApp().appCheckGLError( "TEXTURE GLTEXIMAGE2D");
        glGenerateMipmap(GL_TEXTURE_2D);
         
        // Setup the ST coordinate system
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
         
        // Setup what to do when the texture has to be scaled
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf( GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 8);
        
        // Set dimensions
        this.width = tWidth;
        this.height = tHeight;
        
        return texId;
	}
	
	/**
	 * @return Returns the texture index of the texture
	 */
	public int getTextureIndex(){
		return this.textureIndex;
	}
	
	/**
	 * Unloads the openGL texture
	 */
	public void destroy(){
		if ( this.loaded ) {
			glDeleteTextures( this.getTextureIndex() );
		}
	}
	
	/**
	 * Binds the texture to a shader for rendering if it is loaded
	 */
	public void bind(){
		if( this.loaded ) {
			glActiveTexture( GL_TEXTURE0 );
			glBindTexture( GL_TEXTURE_2D, getTextureIndex());
		}
	}
	
	/**
	 * @return Returns the width of the image
	 */
	public int getWidth(){
	    return this.width;
	}
	
	/**
	 * @return Returns the height of the image
	 */
	public int getHeight(){
	    return this.height;
	}
}
